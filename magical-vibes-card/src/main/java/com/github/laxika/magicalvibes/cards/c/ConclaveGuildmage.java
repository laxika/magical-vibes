package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "162")
public class ConclaveGuildmage extends Card {

    public ConclaveGuildmage() {
        // {G}, {T}: Creatures you control gain trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES)),
                "{G}, {T}: Creatures you control gain trample until end of turn."
        ));

        // {5}{W}, {T}: Create a 2/2 green and white Elf Knight creature token with vigilance.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}{W}",
                List.of(new CreateTokenEffect(
                        1,
                        "Elf Knight",
                        2,
                        2,
                        CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.ELF, CardSubtype.KNIGHT),
                        Set.of(Keyword.VIGILANCE),
                        Set.of()
                )),
                "{5}{W}, {T}: Create a 2/2 green and white Elf Knight creature token with vigilance."
        ));
    }
}
