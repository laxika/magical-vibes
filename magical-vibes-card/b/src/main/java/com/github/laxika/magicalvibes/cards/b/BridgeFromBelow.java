package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "81")
@CardRegistration(set = "MMA", collectorNumber = "74")
@CardRegistration(set = "UMA", collectorNumber = "87")
@CardRegistration(set = "SPG", collectorNumber = "3")
@CardRegistration(set = "YSOS", collectorNumber = "32")
public class BridgeFromBelow extends Card {

    public BridgeFromBelow() {
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.YOU_OWN),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardIsTokenPredicate()),
                        new ConditionalEffect(new SourceCardInGraveyard(), CreateTokenEffect.blackZombie(1))));
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.OPPONENT_OWNS),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new ConditionalEffect(new SourceCardInGraveyard(), new ExileSourceCardFromGraveyardEffect()));
    }
}
