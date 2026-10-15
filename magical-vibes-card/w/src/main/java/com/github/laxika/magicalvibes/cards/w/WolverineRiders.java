package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHC", collectorNumber = "14")
public class WolverineRiders extends Card {

    public WolverineRiders() {
        // At the beginning of each upkeep, create a 1/1 green Elf Warrior creature token.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new CreateTokenEffect(
                "Elf Warrior",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.ELF, CardSubtype.WARRIOR),
                Set.of(),
                Set.of()));

        // Whenever another Elf you control enters, you gain life equal to its toughness.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.ELF),
                        new GainLifeEqualToToughnessEffect()));
    }
}
