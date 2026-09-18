package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetCreatureOrSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "TLE", collectorNumber = "81")
@CardRegistration(set = "TLE", collectorNumber = "173")
public class MonkGyatso extends Card {

    public MonkGyatso() {
        addEffect(EffectSlot.ON_ANOTHER_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                new MayEffect(new AirbendTargetCreatureOrSpellEffect(), "Airbend that creature?"));
    }
}
