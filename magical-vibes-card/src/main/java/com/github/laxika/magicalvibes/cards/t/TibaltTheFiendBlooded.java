package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "161")
public class TibaltTheFiendBlooded extends Card {

    public TibaltTheFiendBlooded() {
        // +1: Draw a card, then discard a card at random.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER, true)),
                "+1: Draw a card, then discard a card at random."
        ));

        // −4: Tibalt deals damage equal to the number of cards in target player's hand to that player.
        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new DealDamageToPlayersEffect(new CardsInHand(CountScope.TARGET_PLAYER), DamageRecipient.TARGET_PLAYER)),
                "−4: Tibalt, the Fiend-Blooded deals damage equal to the number of cards in target player's hand to that player."
        ));

        // −6: Gain control of all creatures until end of turn. Untap them. They gain haste until end of turn.
        // The untap and haste riders run after the steal, so "all creatures" are creatures we now control.
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new GainControlOfAllPermanentsMatchingEffect(new PermanentIsCreaturePredicate(), ControlDuration.END_OF_TURN),
                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES)),
                "−6: Gain control of all creatures until end of turn. Untap them. They gain haste until end of turn."
        ));
    }
}
