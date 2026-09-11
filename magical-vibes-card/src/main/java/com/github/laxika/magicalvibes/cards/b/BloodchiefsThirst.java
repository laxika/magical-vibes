package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "94")
public class BloodchiefsThirst extends Card {

    public BloodchiefsThirst() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{B}"));

        PermanentPredicate creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate()
        ));
        PermanentPredicate baseTarget = new PermanentAllOfPredicate(List.of(
                creatureOrPlaneswalker,
                new PermanentMaxManaValuePredicate(2)
        ));
        target(new PermanentPredicateTargetFilter(
                baseTarget,
                "Target must be a creature or planeswalker with mana value 2 or less",
                creatureOrPlaneswalker
        )).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Kicked(),
                new DestroyTargetPermanentEffect(false),
                new DestroyTargetPermanentEffect(false)
        ));
    }
}
