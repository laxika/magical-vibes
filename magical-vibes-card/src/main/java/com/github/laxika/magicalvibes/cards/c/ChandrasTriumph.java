package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "121")
public class ChandrasTriumph extends Card {

    public ChandrasTriumph() {
        PermanentPredicate creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate()
        ));
        PermanentPredicate creatureOrPlaneswalkerAnOpponentControls = new PermanentAllOfPredicate(List.of(
                creatureOrPlaneswalker,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.CHANDRA)),
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(3, creatureOrPlaneswalkerAnOpponentControls),
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(5, creatureOrPlaneswalkerAnOpponentControls)
        ));
    }
}
