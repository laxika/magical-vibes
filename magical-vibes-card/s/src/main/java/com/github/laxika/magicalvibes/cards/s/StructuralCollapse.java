package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "GTC", collectorNumber = "107")
public class StructuralCollapse extends Card {

    public StructuralCollapse() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                1, new PermanentIsArtifactPredicate(), SacrificeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                1, new PermanentIsLandPredicate(), SacrificeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER));
    }
}
