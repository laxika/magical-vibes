package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "264")
public class TheBiblioplex extends Card {

    public TheBiblioplex() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        )),
                        true
                )),
                "{2}, {T}: Look at the top card of your library. If it's an instant or sorcery card, "
                        + "you may reveal it and put it into your hand. If you don't put the card into "
                        + "your hand, you may put it into your graveyard. Activate only if you have "
                        + "exactly zero or seven cards in hand."
        ).withActivationCondition(
                new AnyOf(List.of(
                        new ControllerHandEmpty(),
                        new AllOf(List.of(new CardsInHandAtLeast(7), new CardsInHandAtMost(7)))
                )),
                "Activate only if you have exactly zero or seven cards in hand"
        ));
    }
}
