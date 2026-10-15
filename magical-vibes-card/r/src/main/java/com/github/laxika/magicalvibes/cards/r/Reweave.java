package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "82")
public class Reweave extends Card {

    public Reweave() {
        // Target permanent's controller sacrifices it. If the player does, they reveal cards from the
        // top of their library until they reveal a permanent card that shares a card type with the
        // sacrificed permanent, put that card onto the battlefield, then shuffle.
        // The empty type set selects the effect's dynamic "shares a card type" mode.
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.SPELL, new SacrificeTargetThenRevealUntilTypeToBattlefieldEffect(Set.of()));

        // Splice onto Arcane {2}{U}{U}
        addEffect(EffectSlot.STATIC, new SpliceEffect(CardSubtype.ARCANE, "{2}{U}{U}"));
    }
}
