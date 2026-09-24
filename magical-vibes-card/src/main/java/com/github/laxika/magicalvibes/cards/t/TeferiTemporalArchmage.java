package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllowLoyaltyActivationAtInstantSpeedEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "125")
@CardRegistration(set = "CMM", collectorNumber = "500")
public class TeferiTemporalArchmage extends Card {

    private static final String EMBLEM_TEXT =
            "You may activate loyalty abilities of planeswalkers you control on any player's turn any time you could cast an instant.";

    public TeferiTemporalArchmage() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(2))),
                "+1: Look at the top two cards of your library. Put one of them into your hand and the other on the bottom of your library."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS)),
                "\u22121: Untap up to four target permanents.",
                null,
                -1,
                null,
                null,
                List.of(
                        TargetFilters.permanent(),
                        TargetFilters.permanent(),
                        TargetFilters.permanent(),
                        TargetFilters.permanent()),
                0,
                4
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new CreateEmblemEffect(List.of(new AllowLoyaltyActivationAtInstantSpeedEffect()), EMBLEM_TEXT)),
                "\u221210: You get an emblem with \"" + EMBLEM_TEXT + "\""
        ));
    }
}
