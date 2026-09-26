package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "247")
public class KayaGhostAssassin extends Card {

    public KayaGhostAssassin() {
        addActivatedAbility(new ActivatedAbility(
                false, null, List.of(
                        FlickerEffect.exileSelfOrTargetReturnAtControllerNextStep(TurnStep.UPKEEP),
                        new LoseLifeEffect(2)),
                "0: Exile Kaya or up to one target creature. Return that card to the battlefield under its owner's control at the beginning of your next upkeep. You lose 2 life.",
                null, 0, null, null,
                List.of(TargetFilters.creature()), 0, 1));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT), new GainLifeEffect(2)),
                "−1: Each opponent loses 2 life and you gain 2 life."));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT), new DrawCardEffect(1)),
                "−2: Each opponent discards a card and you draw a card."));
    }
}
