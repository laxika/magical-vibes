package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "208")
public class ElvishPromenade extends Card {

    public ElvishPromenade() {
        // Create a 1/1 green Elf Warrior creature token for each Elf you control.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER),
                "Elf Warrior", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()));
    }
}
