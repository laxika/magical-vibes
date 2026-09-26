package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromChosenPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "735")
public class ChandraLegacyOfFire extends Card {

    public ChandraLegacyOfFire() {
        PermanentCount planeswalkersYouControl = new PermanentCount(
                new PermanentIsPlaneswalkerPredicate(), CountScope.CONTROLLER);

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new DealDamageToPlayersEffect(
                planeswalkersYouControl, DamageRecipient.EACH_OPPONENT));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new AwardManaEffect(ManaColor.RED, planeswalkersYouControl)),
                "+1: Add {R} for each planeswalker you control."
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(
                        new RemoveCounterFromChosenPermanentsEffect(
                                CounterType.LOYALTY, new PermanentTruePredicate()),
                        new ExileTopCardsMayPlayThisTurnEffect(new EventValue())
                ),
                "0: Remove a loyalty counter from each of any number of permanents you control. "
                        + "Exile that many cards from the top of your library. You may play them this turn."
        ));
    }
}
