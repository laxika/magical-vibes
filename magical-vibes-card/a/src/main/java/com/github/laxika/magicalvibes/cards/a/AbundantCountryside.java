package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECC", collectorNumber = "22")
@CardRegistration(set = "ECC", collectorNumber = "42")
public class AbundantCountryside extends Card {

    public AbundantCountryside() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Add one mana of any color. Spend this mana only to cast a creature spell.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.CREATURE_SPELL_ONLY)),
                "{T}: Add one mana of any color. Spend this mana only to cast a creature spell."
        ));

        // {6}, {T}: Create a 1/1 colorless Shapeshifter creature token with changeling.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(new CreateTokenEffect(
                        1,
                        "Shapeshifter",
                        1,
                        1,
                        null,
                        List.of(CardSubtype.SHAPESHIFTER),
                        Set.of(Keyword.CHANGELING),
                        Set.of()
                )),
                "{6}, {T}: Create a 1/1 colorless Shapeshifter creature token with changeling."
        ));
    }
}
