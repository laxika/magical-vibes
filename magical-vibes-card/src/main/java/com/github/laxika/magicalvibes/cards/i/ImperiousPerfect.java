package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "220")
public class ImperiousPerfect extends Card {

    public ImperiousPerfect() {
        // Other Elves you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ELF))));

        // {G}, {T}: Create a 1/1 green Elf Warrior creature token.
        addActivatedAbility(new ActivatedAbility(
                true, "{G}",
                List.of(new CreateTokenEffect("Elf Warrior", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of())),
                "{G}, {T}: Create a 1/1 green Elf Warrior creature token."
        ));
    }
}
