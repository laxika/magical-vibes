package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "260")
public class AvengersTower extends Card {

    public AvengersTower() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.SUBTYPE_SPELL_OR_ABILITY,
                        CardSubtype.HERO)),
                "{T}: Add one mana of any color. Spend this mana only to cast a Hero spell or to activate an ability of a Hero source."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(
                        3, new CardSubtypePredicate(CardSubtype.HERO))),
                "{4}, {T}: Look at the top three cards of your library. You may reveal a Hero card from among them and put it into your hand. Put the rest on the bottom of your library in any order."
        ));
    }
}
