package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.effect.UntapControlledTapOpponentTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "245")
public class TeferiWhoSlowsTheSunset extends Card {

    public TeferiWhoSlowsTheSunset() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new UntapControlledTapOpponentTargetsEffect(), new GainLifeEffect(2)),
                "+1: Choose up to one target artifact, up to one target creature, and up to one target land. "
                        + "Untap the chosen permanents you control. Tap the chosen permanents you don't control. "
                        + "You gain 2 life.",
                null,
                +1,
                null,
                null,
                List.of(TargetFilters.artifact(), TargetFilters.creature(), TargetFilters.land()),
                0,
                3
        ).withAllowSharedTargets());

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3))),
                "\u22122: Look at the top three cards of your library. Put one of them into your hand and the rest on the bottom of your library in any order."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(
                                new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep.UNTAP),
                                new EmblemStepTriggerEffect(
                                        EmblemTriggerStep.OPPONENT_DRAW_STEP,
                                        List.of(new DrawCardEffect(1)),
                                        "You draw a card during each opponent's draw step."
                                )
                        ),
                        "Untap all permanents you control during each opponent's untap step. You draw a card during each opponent's draw step."
                )),
                "\u22127: You get an emblem with \"Untap all permanents you control during each opponent's untap step\" and \"You draw a card during each opponent's draw step.\""
        ));
    }
}
