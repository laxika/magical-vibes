package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.FirstNoncreatureSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "266")
public class NullstoneGargoyle extends Card {

    public NullstoneGargoyle() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new FirstNoncreatureSpellCastTriggerEffect(List.of(new CounterSpellEffect())));
    }
}
