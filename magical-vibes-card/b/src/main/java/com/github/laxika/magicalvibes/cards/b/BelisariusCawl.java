package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "106")
public class BelisariusCawl extends Card {

    public BelisariusCawl() {
        // {T}, Tap two untapped artifacts you control: Create a 2/2 white Astartes Warrior creature
        // token with vigilance.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsArtifactPredicate(), true),
                        new CreateTokenEffect(
                                "Astartes Warrior", 2, 2, CardColor.WHITE,
                                List.of(CardSubtype.ASTARTES, CardSubtype.WARRIOR),
                                Set.of(Keyword.VIGILANCE), Set.of()
                        )
                ),
                "{T}, Tap two untapped artifacts you control: Create a 2/2 white Astartes Warrior creature token with vigilance."
        ));

        // {T}, Tap X untapped creatures you control: Look at the top X cards of your library. You may
        // reveal an artifact card from among them and put it into your hand. Put the rest on the
        // bottom of your library in a random order.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapMultiplePermanentsCost(new XValue(), new PermanentIsCreaturePredicate(), true),
                        new LookAtTopCardsEffect(
                                new XValue(), new Fixed(1), new CardTypePredicate(CardType.ARTIFACT),
                                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                                LibrarySearchDestination.HAND, true
                        )
                ),
                "{T}, Tap X untapped creatures you control: Look at the top X cards of your library. "
                        + "You may reveal an artifact card from among them and put it into your hand. "
                        + "Put the rest on the bottom of your library in a random order."
        ));
    }
}
