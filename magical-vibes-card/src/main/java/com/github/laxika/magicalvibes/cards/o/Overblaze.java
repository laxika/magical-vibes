package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromTargetPermanentThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BOK", collectorNumber = "114")
public class Overblaze extends Card {

    public Overblaze() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.SPELL, new DoubleDamageFromTargetPermanentThisTurnEffect());
        addEffect(EffectSlot.STATIC, new SpliceEffect(CardSubtype.ARCANE, "{2}{R}{R}"));
    }
}
