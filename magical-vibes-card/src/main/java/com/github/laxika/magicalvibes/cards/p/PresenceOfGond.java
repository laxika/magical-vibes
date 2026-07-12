package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "125")
public class PresenceOfGond extends Card {

    public PresenceOfGond() {
        // Enchant creature.
        // Enchanted creature has "{T}: Create a 1/1 green Elf Warrior creature token."
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new CreateTokenEffect("Elf Warrior", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of())),
                        "{T}: Create a 1/1 green Elf Warrior creature token."
                ),
                GrantScope.ENCHANTED_CREATURE
        ));
    }
}
