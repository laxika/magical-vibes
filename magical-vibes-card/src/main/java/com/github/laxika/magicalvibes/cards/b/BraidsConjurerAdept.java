package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.ActivePlayerPutsCardFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "36")
public class BraidsConjurerAdept extends Card {

    public BraidsConjurerAdept() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new MayEffect(
                new ActivePlayerPutsCardFromHandOntoBattlefieldEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.LAND))),
                        "artifact, creature, or land"),
                "Put an artifact, creature, or land card from your hand onto the battlefield?",
                null,
                MayChoicePlayer.ACTIVE_PLAYER));
    }
}
