package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "157")
public class OrbOfDragonkind extends Card {

    public OrbOfDragonkind() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(
                        2, ManaSpendRestriction.SUBTYPE_SPELL_OR_ABILITY, CardSubtype.DRAGON)),
                "{T}: Add two mana in any combination of colors. Spend this mana only to cast Dragon spells or activate abilities of Dragons."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(
                        new SacrificeSelfCost(),
                        LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                                7, new CardSubtypePredicate(CardSubtype.DRAGON))
                ),
                "{R}, {T}, Sacrifice this artifact: Look at the top seven cards of your library. You may reveal a Dragon card from among them and put it into your hand. Put the rest on the bottom of your library in a random order."
        ));
    }
}
