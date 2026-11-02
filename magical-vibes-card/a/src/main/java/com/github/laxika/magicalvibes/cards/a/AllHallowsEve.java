package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveScreamCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "88")
@CardRegistration(set = "ME3", collectorNumber = "57")
public class AllHallowsEve extends Card {

    public AllHallowsEve() {
        addSpellEffect(ExileSpellEffect.withScreamCounters(2));
        addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.EXILE), TriggerOptions.DEFAULT,
                new RemoveScreamCounterFromExiledCardEffect(
                        getId(),
                        new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                                Integer.MAX_VALUE,
                                new CardTypePredicate(CardType.CREATURE))));
    }
}
