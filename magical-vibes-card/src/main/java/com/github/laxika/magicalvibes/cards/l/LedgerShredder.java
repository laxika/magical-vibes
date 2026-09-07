package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "46")
public class LedgerShredder extends Card {

    public LedgerShredder() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new NthSpellCastTriggerEffect(
                2,
                List.of(new DrawDiscardAndConniveEffect())
        ));
    }
}
