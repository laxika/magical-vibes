package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DTK", collectorNumber = "122")
public class SilumgarButcher extends Card {

    public SilumgarButcher() {
        // Exploit (When this creature enters, you may sacrifice a creature.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExploitEffect(), "Sacrifice a creature?"));

        // When this creature exploits a creature, target creature gets -3/-3 until end of turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_EXPLOIT, new BoostTargetCreatureEffect(-3, -3));
    }
}
