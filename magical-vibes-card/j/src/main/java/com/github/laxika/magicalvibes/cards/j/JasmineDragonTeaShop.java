package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "270")
public class JasmineDragonTeaShop extends Card {

    public JasmineDragonTeaShop() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.SUBTYPE_SPELL_OR_ABILITY,
                        CardSubtype.ALLY)),
                "{T}: Add one mana of any color. Spend this mana only to cast an Ally spell or activate an ability of an Ally source."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new CreateTokenEffect("Ally", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.ALLY), Set.of(), Set.of())),
                "{5}, {T}: Create a 1/1 white Ally creature token."
        ));
    }
}
