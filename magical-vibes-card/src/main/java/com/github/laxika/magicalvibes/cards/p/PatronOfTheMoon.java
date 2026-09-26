package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "45")
public class PatronOfTheMoon extends Card {

    public PatronOfTheMoon() {
        addCastingOption(AlternateHandCast.offering(List.of(
                new ManaCastingCost("{5}{U}{U}"),
                new SacrificePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.MOONFOLK))
        )));
        // {1}: Put up to two land cards from your hand onto the battlefield tapped.
        // "Up to two" is modelled as two independently declinable puts.
        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new MayEffect(new PutCardToBattlefieldEffect(land, "land", true),
                                "Put a land card from your hand onto the battlefield tapped?"),
                        new MayEffect(new PutCardToBattlefieldEffect(land, "land", true),
                                "Put a second land card from your hand onto the battlefield tapped?")
                ),
                "{1}: Put up to two land cards from your hand onto the battlefield tapped."
        ));
    }
}
