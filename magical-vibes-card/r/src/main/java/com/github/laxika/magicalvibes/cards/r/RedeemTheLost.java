package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOR", collectorNumber = "21")
public class RedeemTheLost extends Card {

    public RedeemTheLost() {
        // Target creature you control gains protection from the color of your choice until end of turn.
        // Clash with an opponent. If you win, return Redeem the Lost to its owner's hand.
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new GrantProtectionChoiceUntilEndOfTurnEffect())
                .addSpellEffect(new ClashEffect(ReturnToHandEffect.selfSpell()));
    }
}
