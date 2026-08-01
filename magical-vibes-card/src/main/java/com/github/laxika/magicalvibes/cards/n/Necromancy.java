package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeAuraReanimateFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.FlashCastWithCleanupSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureOnLeaveEffect;

@CardRegistration(set = "VIS", collectorNumber = "64")
public class Necromancy extends Card {

    public Necromancy() {
        // Mirage flash clause: cast as though it had flash; sacrificed at next cleanup if cast
        // when a sorcery couldn't have been cast.
        addEffect(EffectSlot.STATIC, new FlashCastWithCleanupSacrificeEffect());
        // ETB: become an Aura, put target creature card from a graveyard onto the battlefield
        // under your control, and attach this to it (intervening-if: still on the battlefield).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeAuraReanimateFromGraveyardEffect());
        // When this leaves the battlefield, that creature's controller sacrifices it.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new SacrificeEnchantedCreatureOnLeaveEffect());
    }
}
