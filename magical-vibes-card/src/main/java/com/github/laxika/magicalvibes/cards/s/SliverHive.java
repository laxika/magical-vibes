package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorSubtypeSpellManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "247")
public class SliverHive extends Card {

    public SliverHive() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Add one mana of any color. Spend this mana only to cast a Sliver spell.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorSubtypeSpellManaEffect(CardSubtype.SLIVER)),
                "{T}: Add one mana of any color. Spend this mana only to cast a Sliver spell."
        ));

        // {5}, {T}: Create a 1/1 colorless Sliver creature token. Activate only if you control a Sliver.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new CreateTokenEffect("Sliver", 1, 1, null,
                        List.of(CardSubtype.SLIVER), Set.of(), Set.of())),
                "{5}, {T}: Create a 1/1 colorless Sliver creature token. Activate only if you control a Sliver.",
                CardSubtype.SLIVER,
                1
        ));
    }
}
