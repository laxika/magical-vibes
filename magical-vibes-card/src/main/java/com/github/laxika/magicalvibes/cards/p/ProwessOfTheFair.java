package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "136")
public class ProwessOfTheFair extends Card {

    public ProwessOfTheFair() {
        // Whenever another nontoken Elf is put into your graveyard from the battlefield,
        // you may create a 1/1 green Elf Warrior creature token.
        // ON_ALLY_CREATURE_DIES supports the conditional-may unwrap; the predicate enforces
        // both the Elf subtype and the "nontoken" clause. Prowess itself is a non-creature
        // Elf, so it can never be the triggering creature ("another" is satisfied automatically).
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardAllOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.ELF),
                        new CardNotPredicate(new CardIsTokenPredicate()))),
                new MayEffect(
                        new CreateTokenEffect("Elf Warrior", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()),
                        "Create a 1/1 green Elf Warrior creature token?")
        ));
    }
}
