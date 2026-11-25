package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForTargetControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ONS", collectorNumber = "193")
public class ChainOfPlasma extends Card {

    public ChainOfPlasma() {
        addSpellEffect(DealDamageEffect.toAnyTarget(3));
        addSpellEffect(new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        new MayEffect(
                                new CopyThisSpellForTargetControllerEffect(),
                                "Copy Chain of Plasma?",
                                null,
                                MayChoicePlayer.TARGET_PLAYER_OR_PERMANENT_CONTROLLER
                        ),
                        "a card",
                        Subjects.allOf(Subjects.targetPlayerDeclaredElsewhere(), Subjects.controllerOf(Subjects.target())),
                        true
                ),
                "Discard a card?",
                null,
                MayChoicePlayer.TARGET_PLAYER_OR_PERMANENT_CONTROLLER
        ));
    }
}
