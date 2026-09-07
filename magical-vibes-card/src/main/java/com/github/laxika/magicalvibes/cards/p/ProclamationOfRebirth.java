package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "15")
public class ProclamationOfRebirth extends Card {

    public ProclamationOfRebirth() {
        CardAllOfPredicate creatureWithManaValueOneOrLess = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMaxManaValuePredicate(1)
        ));

        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                creatureWithManaValueOneOrLess, 3, false, false));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{5}{W}",
                List.of(new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        creatureWithManaValueOneOrLess, 1, false, false)),
                "Forecast \u2014 {5}{W}, Reveal this card from your hand: Return target creature card with mana value 1 or less "
                        + "from your graveyard to the battlefield. Activate only during your upkeep and only once each turn.",
                null,
                null,
                1,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withRevealsSourceFromHand());
    }
}
