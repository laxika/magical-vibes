package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ONS", collectorNumber = "130")
public class CabalExecutioner extends Card {

    public CabalExecutioner() {
        addMorph("{3}{B}{B}");
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                        SacrificeRecipient.TARGET_PLAYER));
    }
}
