package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "209")
public class XenagosTheReveler extends Card {

    public XenagosTheReveler() {
        // +1: Add X mana in any combination of {R} and/or {G}, where X is the number of creatures
        // you control.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new AwardManaOfColorsEffect(
                        List.of(ManaColor.RED, ManaColor.GREEN),
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER))),
                "+1: Add X mana in any combination of {R} and/or {G}, where X is the number of creatures you control."
        ));

        // 0: Create a 2/2 red and green Satyr creature token with haste.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new CreateTokenEffect(
                        1, "Satyr", 2, 2, CardColor.RED, Set.of(CardColor.RED, CardColor.GREEN),
                        List.of(CardSubtype.SATYR), Set.of(Keyword.HASTE), Set.of())),
                "0: Create a 2/2 red and green Satyr creature token with haste."
        ));

        // −6: Exile the top seven cards of your library. You may put any number of creature and/or
        // land cards from among them onto the battlefield.
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new LookAtTopCardsEffect(
                        new Fixed(7), new Fixed(7),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.LAND))),
                        LookDestination.EXILE, true, LibrarySearchDestination.BATTLEFIELD, true)),
                "−6: Exile the top seven cards of your library. You may put any number of creature and/or land cards from among them onto the battlefield."
        ));
    }
}
