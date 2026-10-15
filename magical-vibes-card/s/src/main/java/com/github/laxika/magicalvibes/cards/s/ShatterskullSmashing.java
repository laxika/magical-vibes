package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "161")
public class ShatterskullSmashing extends Card {

    public ShatterskullSmashing() {
        setBackFaceCard(new ShatterskullTheHammerPass());
        setModalDoubleFaced(true);

        PermanentPredicate creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate()));
        PermanentPredicateTargetFilter targetFilter = new PermanentPredicateTargetFilter(
                creatureOrPlaneswalker,
                "Target must be a creature or planeswalker.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Shatterskull Smashing deals X damage divided as you choose among up to two target creatures and/or planeswalkers",
                        List.of(new ConditionalReplacementEffect(
                                new SpellXAtLeast(6),
                                dividedDamage(new XValue()),
                                dividedDamage(new Scaled(new XValue(), 2)))),
                        targetFilter, null, 0, 2, false, null),
                new ChooseOneEffect.ChooseOneOption("Shatterskull, the Hammer Pass", List.of())
        )));
    }

    private static DealDividedDamageEffect dividedDamage(DynamicAmount amount) {
        return new DealDividedDamageEffect(
                amount,
                null,
                DivisionMode.CHOSEN,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                2,
                false,
                false,
                false,
                false,
                false,
                true);
    }

    @Override
    public String getBackFaceClassName() {
        return "ShatterskullTheHammerPass";
    }
}
