package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.condition.SourceCardSuspended;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersOnSuspendedCardEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "173")
public class PardicDragon extends Card {

    public PardicDragon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{R}{R}",
                List.of(),
                "Suspend 2—{R}{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(2));

        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new ConditionalEffect(
                new SourceCardSuspended(),
                new MayEffect(
                        new PutTimeCountersOnSuspendedCardEffect(1),
                        "Put a time counter on Pardic Dragon?",
                        null,
                        MayChoicePlayer.TRIGGERING_SPELL_CONTROLLER
                )
        ));
    }
}
