package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "7")
public class WretchedGryff extends Card {

    public WretchedGryff() {
        // Emerge {5}{U} — sacrifice a creature and pay the emerge cost reduced by that creature's
        // mana value (generic only; colored components cannot be reduced).
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{5}{U}"),
                new SacrificePermanentsCost(1, new PermanentIsCreaturePredicate())
        ), true));

        // When you cast this spell, draw a card.
        addEffect(EffectSlot.ON_SELF_CAST, new DrawCardEffect(1));
    }
}
