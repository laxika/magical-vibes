package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileMatchingCardsFromGraveyardAndLibraryMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "135")
public class ChandraHeartOfFire extends Card {

    public ChandraHeartOfFire() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new DiscardEffect(new CardsInHand(CountScope.CONTROLLER), DiscardRecipient.CONTROLLER),
                        new ExileTopCardMayPlayThisTurnEffect(3, false)
                ),
                "+1: Discard your hand, then exile the top three cards of your library. Until end of turn, you may play cards exiled this way."
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DealDamageToAnyTargetEffect(2)),
                "+1: Chandra deals 2 damage to any target."
        ));

        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(
                        new ExileMatchingCardsFromGraveyardAndLibraryMayCastThisTurnEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardColorPredicate(CardColor.RED),
                                        new CardAnyOfPredicate(List.of(
                                                new CardTypePredicate(CardType.INSTANT),
                                                new CardTypePredicate(CardType.SORCERY)
                                        ))
                                ))),
                        new AwardManaEffect(ManaColor.RED, 6)
                ),
                "−9: Search your graveyard and library for any number of red instant and/or sorcery cards, exile them, then shuffle. You may cast them this turn. Add six {R}."
        ));
    }
}
