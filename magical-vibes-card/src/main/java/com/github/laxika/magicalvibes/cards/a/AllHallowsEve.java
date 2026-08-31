package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveScreamCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LEG", collectorNumber = "88")
public class AllHallowsEve extends Card {

    public AllHallowsEve() {
        addEffect(EffectSlot.SPELL, ExileSpellEffect.withScreamCounters(2));
        addEffect(EffectSlot.EXILED_SCREAM_COUNTER_UPKEEP_TRIGGERED,
                new RemoveScreamCounterFromExiledCardEffect(
                        getId(),
                        new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                                Integer.MAX_VALUE,
                                new CardTypePredicate(CardType.CREATURE))));
    }
}
