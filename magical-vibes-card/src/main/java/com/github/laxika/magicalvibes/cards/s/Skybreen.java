package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsSharingCardTypeWithTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "OHOP", collectorNumber = "35")
public class Skybreen extends Card {

    public Skybreen() {
        addEffect(EffectSlot.STATIC, PlayWithTopCardRevealedEffect.forAllPlayers());
        addEffect(EffectSlot.STATIC, new CantCastSpellsSharingCardTypeWithTopCardEffect());
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.CHAOS_TRIGGERED,
                new LoseLifeEffect(new CardsInHand(CountScope.TARGET_PLAYER),
                        LoseLifeRecipient.TARGET_PLAYER));
    }
}
