package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllowLoyaltyActivationAtInstantSpeedEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "19")
public class TeferiTemporalArchmage extends Card {

    public TeferiTemporalArchmage() {
        // +1: Look at the top two cards of your library. Put one of them into your hand and the
        // other on the bottom of your library.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(2))),
                "+1: Look at the top two cards of your library. Put one of them into your hand and "
                        + "the other on the bottom of your library."
        ));

        // −1: Untap up to four target permanents.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS)),
                "−1: Untap up to four target permanents.",
                null,
                -1,
                null,
                null,
                List.of(TargetFilters.permanent(), TargetFilters.permanent(),
                        TargetFilters.permanent(), TargetFilters.permanent()),
                0,
                4
        ));

        // −10: You get an emblem with the ability to activate your planeswalkers' loyalty abilities
        // on any player's turn any time you could cast an instant.
        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new CreateEmblemEffect(
                        List.of(new AllowLoyaltyActivationAtInstantSpeedEffect()),
                        "You may activate loyalty abilities of planeswalkers you control on any player's "
                                + "turn any time you could cast an instant.")),
                "−10: You get an emblem with \"You may activate loyalty abilities of planeswalkers you "
                        + "control on any player's turn any time you could cast an instant.\""
        ));
    }
}
