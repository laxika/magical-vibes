package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "237")
public class VesselOfNascency extends Card {

    public VesselOfNascency() {
        CardAnyOfPredicate permanentCard = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardTypePredicate(CardType.LAND),
                new CardTypePredicate(CardType.PLANESWALKER)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        LookAtTopCardsEffect.mayRevealOneToHandRestToGraveyard(4, permanentCard)
                ),
                "{1}{G}, Sacrifice this enchantment: Reveal the top four cards of your library. "
                        + "You may put an artifact, creature, enchantment, land, or planeswalker card "
                        + "from among them into your hand. Put the rest into your graveyard."
        ));
    }
}
