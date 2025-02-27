package pinball;

public enum ShapeType
{
    BOX("BOX"), CIRCLE("CIRCLE"), LINE("LINE");
    private String shapeName;

    private ShapeType(String shapeName) {
        this.shapeName = shapeName;
    }

    public String getShapeName() {
        return shapeName;
    }
}
