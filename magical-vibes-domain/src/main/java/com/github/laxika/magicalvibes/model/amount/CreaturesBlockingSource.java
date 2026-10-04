package com.github.laxika.magicalvibes.model.amount;

/** The number of creatures currently blocking the source, or the referenced target for a delayed trigger. */
public record CreaturesBlockingSource(boolean useTarget) implements DynamicAmount {

    public CreaturesBlockingSource() {
        this(false);
    }
}
