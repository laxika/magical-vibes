package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.condition.NotCondition;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "10")
public class DewdropCure extends Card {

    public DewdropCure() {
        CardAllOfPredicate creatureCards = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMaxManaValuePredicate(2)
        ));

        addEffect(EffectSlot.STATIC, new GiftEffect(2));
        target(new GraveyardCardPredicateTargetFilter(
                creatureCards, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 3)
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                        new EachOtherPlayerDrawsCardEffect(1)))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                        new ReturnTargetCardsFromGraveyardToBattlefieldEffect(creatureCards, 3, false, false)))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(new GiftPromised()),
                        new ReturnTargetCardsFromGraveyardToBattlefieldEffect(creatureCards, 2, false, false)));
    }
}
