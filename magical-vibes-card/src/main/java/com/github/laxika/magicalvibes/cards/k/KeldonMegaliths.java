package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "170")
public class KeldonMegaliths extends Card {

    public KeldonMegaliths() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {R}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        // Hellbent — {1}{R}, {T}: This land deals 1 damage to any target. Activate only if you
        // have no cards in hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new DealDamageToAnyTargetEffect(1)),
                "Hellbent — {1}{R}, {T}: This land deals 1 damage to any target. Activate only if you have no cards in hand."
        ).withMaxCardsInHand(0));
    }
}
