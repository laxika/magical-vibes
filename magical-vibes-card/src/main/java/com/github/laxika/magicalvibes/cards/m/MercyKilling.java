package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "231")
public class MercyKilling extends Card {

    public MercyKilling() {
        // Target creature's controller sacrifices it, then creates X 1/1 green and white Elf Warrior
        // creature tokens, where X is that creature's power.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.SPELL, new SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect(
                new CreateTokenEffect(1, "Elf Warrior", 1, 1, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.ELF, CardSubtype.WARRIOR))
        ));
    }
}
