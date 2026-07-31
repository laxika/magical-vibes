package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageToTargetAndAddToughnessCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "16")
public class ScarsOfTheVeteran extends Card {

    public ScarsOfTheVeteran() {
        // You may exile a white card from your hand rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(
                new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.WHITE), "white"))));

        // Prevent the next 7 damage that would be dealt to any target this turn. If it's a
        // creature, put a +0/+1 counter on it for each 1 damage prevented this way at the
        // beginning of the next end step.
        addEffect(EffectSlot.SPELL, new PreventNextDamageToTargetAndAddToughnessCountersEffect(7, true));
    }
}
