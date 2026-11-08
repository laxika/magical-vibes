package com.github.laxika.magicalvibes.model.condition;

/**
 * "If a casualty cost was paid for it": the intervening "if" of casualty's "when you cast this spell, … copy it"
 * (CR 702.153a). Read from the spell as its cast trigger triggers, and from the trigger's stack entry, which records
 * the spell's payment, as it resolves.
 */
public record CasualtyCostPaid() implements Condition {

    @Override
    public String conditionName() {
        return "casualty cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "its casualty cost was not paid";
    }
}
