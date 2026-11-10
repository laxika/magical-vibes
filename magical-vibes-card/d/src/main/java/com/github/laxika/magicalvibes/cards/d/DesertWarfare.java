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
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "64")
@CardRegistration(set = "M3C", collectorNumber = "98")
@CardRegistration(set = "M3C", collectorNumber = "116")
public class DesertWarfare extends Card {

    public DesertWarfare() {
        RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect returnDesert =
                new RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect();

        // Whenever you sacrifice a Desert and whenever a Desert card is put into your graveyard from your
        // hand or library, put that card onto the battlefield under your control at the beginning of your
        // next end step. "That card" is the stack entry's triggering card; each Desert card triggers once.
        addTrigger(Triggers.sacrificed(
                        new PermanentHasSubtypePredicate(CardSubtype.DESERT),
                        ObjectRelation.YOU_CONTROL),
                returnDesert);
        addTrigger(Triggers.putIntoGraveyardFrom(Set.of(Zone.HAND, Zone.LIBRARY),
                        new PermanentAllOfPredicate(List.of(Triggers.NONTOKEN,
                                new PermanentHasSubtypePredicate(CardSubtype.DESERT))),
                        ObjectRelation.YOU_OWN),
                returnDesert);

        PermanentHasSubtypePredicate desertPermanent = new PermanentHasSubtypePredicate(CardSubtype.DESERT);
        PermanentCount deserts = new PermanentCount(desertPermanent, CountScope.CONTROLLER);
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControlsPermanentCount(5, desertPermanent)),
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
                        Set.of()));
    }
}
