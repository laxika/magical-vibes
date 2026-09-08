package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "VOW", collectorNumber = "248")
public class SkullSkaab extends Card {

    public SkullSkaab() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExploitEffect(), "Sacrifice a creature?"));
        addEffect(EffectSlot.ON_ALLY_CREATURE_EXPLOITS_NONTOKEN_CREATURE,
                CreateTokenEffect.blackZombie(1));
    }
}
