package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "BLB", collectorNumber = "188")
public class PeerlessRecycling extends Card {

    public PeerlessRecycling() {
        CardIsPermanentPredicate permanentCards = new CardIsPermanentPredicate();

        addEffect(EffectSlot.STATIC, new GiftEffect(1));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                new EachOtherPlayerDrawsCardEffect(1)));
        targetWhenGiftPromised(new GraveyardCardPredicateTargetFilter(
                permanentCards, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 1, 2, 2)
                .addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToHandEffect(
                        permanentCards, 2));
    }
}
