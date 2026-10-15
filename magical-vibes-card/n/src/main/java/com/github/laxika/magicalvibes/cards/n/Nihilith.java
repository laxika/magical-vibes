package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardSuspended;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterFromExiledCardEffect;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "72")
public class Nihilith extends Card {

    public Nihilith() {
        addEffect(EffectSlot.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE,
                new ConditionalEffect(
                        new SourceCardSuspended(),
                        new MayEffect(new RemoveTimeCounterFromExiledCardEffect(getId()),
                                "Remove a time counter from Nihilith?")));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(),
                "Suspend 7-{1}{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(7));
    }
}
