package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "136")
@CardRegistration(set = "KHC", collectorNumber = "53")
public class ProwessOfTheFair extends Card {

    public ProwessOfTheFair() {
        // Whenever another nontoken Elf is put into your graveyard from the battlefield, you may create a
        // 1/1 green Elf Warrior creature token. Elves of any card type count, so this watches nontoken
        // permanents going to the graveyard rather than only creatures dying.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.ELF),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        new MayEffect(
                                new CreateTokenEffect("Elf Warrior", 1, 1, CardColor.GREEN,
                                        List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()),
                                "Create a 1/1 green Elf Warrior creature token?")));
    }
}
