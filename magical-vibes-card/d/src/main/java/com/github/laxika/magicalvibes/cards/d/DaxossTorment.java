package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C15", collectorNumber = "18")
public class DaxossTorment extends Card {

    public DaxossTorment() {
        // Constellation — Whenever this enchantment or another enchantment you control enters,
        // this enchantment becomes a 5/5 Demon creature with flying and haste until end of turn.
        AnimatePermanentsEffect animation = new AnimatePermanentsEffect(
                5, 5, List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING, Keyword.HASTE));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, animation);
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, animation);
    }
}
