package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEM", collectorNumber = "93")
public class MoggAlarm extends Card {

    public MoggAlarm() {
        // You may sacrifice two Mountains rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(
                new SacrificePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)))));

        // Create two 1/1 red Goblin creature tokens.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
