package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.Ribbons;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

/**
 * Cut // Ribbons — front half (Cut).
 * Sorcery — Cut deals 4 damage to target creature.
 * Back half (Ribbons) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "223")
public class CutRibbons extends Card {

    public CutRibbons() {
        setBackFaceCard(new Ribbons());

        // Cut deals 4 damage to target creature.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));
    }

    @Override
    public String getBackFaceClassName() {
        return "Ribbons";
    }
}
