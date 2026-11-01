package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllMatchingCardsFromLibraryWithSourceThenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "14")
public class CalderaBreaker extends Card {

    public CalderaBreaker() {
        PermanentPredicate creatureOrPlaneswalkerAnOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileAllMatchingCardsFromLibraryWithSourceThenEffect(
                        new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                                new EventValue(), creatureOrPlaneswalkerAnOpponentControls)));
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new ReturnAllCardsExiledWithSourceEffect(),
                new ConjureCardNamedIntoLibraryEffect("MIR", "202", 4)));
    }
}
