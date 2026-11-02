package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeAuraReanimateFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.FlashCastWithCleanupSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "64")
@CardRegistration(set = "AA3", collectorNumber = "9")
@CardRegistration(set = "MKC", collectorNumber = "131")
public class Necromancy extends Card {

    public Necromancy() {
        // Mirage flash clause: cast as though it had flash; sacrificed at next cleanup if cast
        // when a sorcery couldn't have been cast.
        addStaticEffect(new FlashCastWithCleanupSacrificeEffect());
        // ETB: become an Aura, put target creature card from a graveyard onto the battlefield
        // under your control, and attach this to it (intervening-if: still on the battlefield).
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeAuraReanimateFromGraveyardEffect());
        // When this leaves the battlefield, that creature's controller sacrifices it.
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new SacrificeEnchantedCreatureOnLeaveEffect());
    }
}
