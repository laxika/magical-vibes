package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "231")
public class MercyKilling extends Card {

    public MercyKilling() {
        // Target creature's controller sacrifices it, then creates X 1/1 green and white Elf Warrior
        // creature tokens, where X is that creature's power: its last-known power once sacrificed, its power if it
        // couldn't be (the tokens don't depend on the sacrifice).
        target(TargetFilters.creature())
                .addSpellEffect(new SacrificeSelfEffect(Subjects.target(), Subjects.controllerOf(Subjects.target())))
                .addSpellEffect(new CreateTokenEffect(1, "Elf Warrior", 1, 1, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.ELF, CardSubtype.WARRIOR))
                        .withAmount(new TargetPower())
                        .forController(Subjects.controllerOf(Subjects.target())));
    }
}
