package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherCreatureEffect;

@CardRegistration(set = "GPT", collectorNumber = "117")
public class InkTreaderNephilim extends Card {

    public InkTreaderNephilim() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CopySpellForEachOtherCreatureEffect());
    }
}
