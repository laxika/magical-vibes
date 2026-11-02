package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "64")
@CardRegistration(set = "M3C", collectorNumber = "98")
@CardRegistration(set = "M3C", collectorNumber = "116")
public class DesertWarfare extends Card {

    public DesertWarfare() {
        CardSubtypePredicate desertCard = new CardSubtypePredicate(CardSubtype.DESERT);
        RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect returnDesert =
                new RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect(desertCard);

        // Whenever you sacrifice a Desert, or a Desert card is put into your graveyard from your
        // hand or library, return that card to the battlefield under your control at the next end step.
        addTrigger(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.DESERT), returnDesert));
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.ANOTHER_YOU_OWN),
                returnDesert);
        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN,
                ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE, returnDesert);

        PermanentHasSubtypePredicate desertPermanent = new PermanentHasSubtypePredicate(CardSubtype.DESERT);
        PermanentCount deserts = new PermanentCount(desertPermanent, CountScope.CONTROLLER);
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new ConditionalEffect(
                new ControlsPermanentCount(5, desertPermanent),
                new CreateTokenEffect(
                        CardType.CREATURE,
                        deserts,
                        "Sand Warrior",
                        1,
                        1,
                        CardColor.RED,
                        Set.of(CardColor.RED, CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.WARRIOR),
                        Set.of(Keyword.HASTE),
                        Set.of(),
                        false,
                        false,
                        List.of(),
                        List.of(),
                        false,
                        false,
                        false,
                        0,
                        Set.of())));
    }
}
