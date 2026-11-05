package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "81")
@CardRegistration(set = "MMA", collectorNumber = "74")
@CardRegistration(set = "UMA", collectorNumber = "87")
@CardRegistration(set = "SPG", collectorNumber = "3")
@CardRegistration(set = "YSOS", collectorNumber = "32")
public class BridgeFromBelow extends Card {

    public BridgeFromBelow() {
        addTrigger(new TriggerCondition(Triggers.dies(
                                new PermanentAllOfPredicate(List.of(
                                        Triggers.CREATURE,
                                        new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                                ObjectRelation.YOU_OWN),
                        AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()),
                CreateTokenEffect.blackZombie(1));
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.OPPONENT_OWNS),
                AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()),
                new ExileSourceCardFromGraveyardEffect());
    }
}
