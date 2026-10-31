package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PartyAlwaysFullEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1539")
public class PinkiePie extends Card {

    public PinkiePie() {
        // Card art is not represented by the engine, so every controller spell is treated as
        // matching the "smile in its art" condition.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(CreateTokenEffect.ofTreasureToken(1, true))
        ));
        addStaticEffect(new PartyAlwaysFullEffect());
    }
}
