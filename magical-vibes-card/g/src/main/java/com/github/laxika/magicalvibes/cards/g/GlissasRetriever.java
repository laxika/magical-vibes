package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithAtLeastPoisonCounters;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "ONC", collectorNumber = "18")
@CardRegistration(set = "ONC", collectorNumber = "56")
public class GlissasRetriever extends Card {

    public GlissasRetriever() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentPowerAtMostPredicate(2)));
        addEffect(EffectSlot.ON_DEATH, new ExileSourceCardFromGraveyardThenEffect(
                ReturnTargetCardsFromGraveyardToHandEffect.forTriggeredAbility(
                        new CardTruePredicate(), new OpponentsWithAtLeastPoisonCounters(3))));
    }
}
