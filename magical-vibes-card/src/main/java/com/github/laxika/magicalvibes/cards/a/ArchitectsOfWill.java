package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfTargetLibraryEffect;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "17")
public class ArchitectsOfWill extends Card {

    public ArchitectsOfWill() {
        // When this creature enters, look at the top three cards of target player's library, then put
        // them back in any order. The effect's targetSpec() is a benign PLAYER target, so no explicit
        // target(...) call is needed.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReorderTopCardsOfTargetLibraryEffect(3));

        // Cycling {U/B} ({U/B}, Discard this card: Draw a card.) — the discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{U/B}",
                List.of(new DrawCardEffect(1)),
                "Cycling {U/B} ({U/B}, Discard this card: Draw a card.)"));
    }
}
