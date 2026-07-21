package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "6")
public class ItOfTheHorridSwarm extends Card {

    public ItOfTheHorridSwarm() {
        // Emerge {6}{G} — sacrifice a creature and pay the emerge cost reduced by that
        // creature's mana value (generic only; colored components cannot be reduced).
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{6}{G}"),
                new SacrificePermanentsCost(1, new PermanentIsCreaturePredicate())
        ), true));

        // When you cast this spell, create two 1/1 green Insect creature tokens.
        addEffect(EffectSlot.ON_SELF_CAST, new CreateTokenEffect(2, "Insect", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.INSECT), Set.of(), Set.of()));
    }
}
