package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowPlayMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "162")
public class GaeasWill extends Card {

    public GaeasWill() {
        addEffect(EffectSlot.SPELL, new AllowPlayMatchingCardsFromGraveyardThisTurnEffect(new CardTruePredicate()));
        addEffect(EffectSlot.SPELL, new ExileOwnCardsInsteadOfGraveyardUntilEndOfTurnEffect());
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(),
                "Suspend 4\u2014{G}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
