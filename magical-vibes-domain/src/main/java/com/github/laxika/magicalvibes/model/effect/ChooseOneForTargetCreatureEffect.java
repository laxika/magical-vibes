package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * A modal ("choose one") activated ability whose every mode applies to the same target creature
 * (Nature's Blessing).
 *
 * <p>{@link ChooseOneEffect} declares no target, so an ability holding one is never offered a
 * target at activation. This variant declares a creature target spec, so the target is chosen as
 * the ability is activated and rides on the stack entry; the mode itself is picked as the ability
 * resolves via the shared {@code ChoiceContext.ChooseModeChoice} flow, and the chosen mode's
 * effects — all of them {@code GrantScope.TARGET} / target-permanent effects — read that same
 * target. Because every mode targets the same object, picking the mode at resolution rather than at
 * activation cannot change which targets are legal.</p>
 *
 * @param options the modes, in card-text order
 */
public record ChooseOneForTargetCreatureEffect(List<ChooseOneEffect.ChooseOneOption> options) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
