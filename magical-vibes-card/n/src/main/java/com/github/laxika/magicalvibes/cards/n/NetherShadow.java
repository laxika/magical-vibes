package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.CardsAboveSelfInGraveyard;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "183")
@CardRegistration(set = "4ED", collectorNumber = "149")
@CardRegistration(set = "SUM", collectorNumber = "118")
@CardRegistration(set = "ME1", collectorNumber = "77")
@CardRegistration(set = "3ED", collectorNumber = "118")
@CardRegistration(set = "2ED", collectorNumber = "117")
@CardRegistration(set = "LEA", collectorNumber = "116")
@CardRegistration(set = "LEB", collectorNumber = "117")
public class NetherShadow extends Card {

    public NetherShadow() {
        addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new CardsAboveSelfInGraveyard(3, new CardTypePredicate(CardType.CREATURE))),
                new MayEffect(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build(),
                        "Put Nether Shadow onto the battlefield from your graveyard?"));
    }
}
