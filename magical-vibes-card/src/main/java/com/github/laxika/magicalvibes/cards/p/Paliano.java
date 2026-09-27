package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NoMonarch;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "62")
public class Paliano extends Card {

    public Paliano() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null,
                        new ConditionalEffect(new NoMonarch(), new BecomeMonarchEffect()),
                        false,
                        true));

        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new CreateTokenEffect("Assassin", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.ASSASSIN), Set.of(Keyword.DEATHTOUCH, Keyword.HASTE), Set.of()));
    }
}
