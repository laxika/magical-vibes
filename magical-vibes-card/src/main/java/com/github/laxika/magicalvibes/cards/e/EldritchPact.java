package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "HBG", collectorNumber = "153")
public class EldritchPact extends Card {

    public EldritchPact() {
        var cardsInTargetGraveyard = new CardsInGraveyard(new CardTruePredicate(), CountScope.TARGET_PLAYER);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ))
                .addEffect(EffectSlot.SPELL, new DrawCardForTargetPlayerEffect(
                        cardsInTargetGraveyard, false, true))
                .addEffect(EffectSlot.SPELL, new LoseLifeEffect(
                        cardsInTargetGraveyard, LoseLifeRecipient.TARGET_PLAYER));
    }
}
