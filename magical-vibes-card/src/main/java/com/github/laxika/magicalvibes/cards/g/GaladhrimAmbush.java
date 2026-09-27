package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "38")
@CardRegistration(set = "LTC", collectorNumber = "121")
public class GaladhrimAmbush extends Card {

    public GaladhrimAmbush() {
        // Create one 1/1 green Elf Warrior creature token for each attacking creature.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER),
                "Elf Warrior", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()));

        // Prevent all combat damage that would be dealt this turn by non-Elf creatures.
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatExcept(
                new PermanentHasSubtypePredicate(CardSubtype.ELF)));
    }
}
