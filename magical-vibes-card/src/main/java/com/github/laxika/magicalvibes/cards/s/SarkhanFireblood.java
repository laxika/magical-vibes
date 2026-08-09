package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "154")
public class SarkhanFireblood extends Card {

    public SarkhanFireblood() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?")),
                "+1: You may discard a card. If you do, draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new AwardAnyColorManaEffect(1, ManaSpendRestriction.SUBTYPE_SPELL, CardSubtype.DRAGON),
                        new AwardAnyColorManaEffect(1, ManaSpendRestriction.SUBTYPE_SPELL, CardSubtype.DRAGON)
                ),
                "+1: Add two mana in any combination of colors. Spend this mana only to cast Dragon spells."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateTokenEffect(4, "Dragon", 5, 5, CardColor.RED,
                        List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of())),
                "\u22127: Create four 5/5 red Dragon creature tokens with flying."
        ));
    }
}
