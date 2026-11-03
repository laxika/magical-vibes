package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NoMonarch;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "62")
public class Paliano extends Card {

    public Paliano() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                TriggerOptions.ONE_OR_MORE.withInterveningIf(new NoMonarch()),
                new BecomeMonarchEffect());

        addTrigger(Triggers.chaosEnsues(),
                new CreateTokenEffect("Assassin", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.ASSASSIN), Set.of(Keyword.DEATHTOUCH, Keyword.HASTE), Set.of()));
    }
}
