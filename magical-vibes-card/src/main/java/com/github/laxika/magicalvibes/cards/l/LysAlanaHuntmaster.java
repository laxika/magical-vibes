package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "229")
public class LysAlanaHuntmaster extends Card {

    public LysAlanaHuntmaster() {
        // Whenever you cast an Elf spell, you may create a 1/1 green Elf Warrior creature token.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.ELF),
                        List.of(new CreateTokenEffect(
                                "Elf Warrior", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()))
                ),
                "Create a 1/1 green Elf Warrior creature token?"
        ));
    }
}
