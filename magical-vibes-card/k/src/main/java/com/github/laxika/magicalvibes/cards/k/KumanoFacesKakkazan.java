package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EtchingOfKumano;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCountersToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "152")
public class KumanoFacesKakkazan extends Card {

    public KumanoFacesKakkazan() {
        setBackFaceCard(new EtchingOfKumano());

        addSimultaneousTrigger(Triggers.chapter(1),
                DealDamageEffect.to(Subjects.eachOpponent(), 1),
                DealDamageEffect.toEach(
                        Subjects.controlledBy(Subjects.eachPlayer(),
                                new PermanentAllOfPredicate(List.of(
                                new PermanentIsPlaneswalkerPredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))), 1));
        addTrigger(Triggers.chapter(2), RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                new CardTypePredicate(CardType.CREATURE), List.of(new GrantAdditionalCountersToCastSpellEffect(new Fixed(1)))));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "EtchingOfKumano";
    }
}
