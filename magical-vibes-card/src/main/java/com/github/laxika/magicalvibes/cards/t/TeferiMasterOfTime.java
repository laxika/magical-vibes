package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowLoyaltyActivationAtInstantSpeedEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "75")
public class TeferiMasterOfTime extends Card {

    public TeferiMasterOfTime() {
        addEffect(EffectSlot.STATIC, new AllowLoyaltyActivationAtInstantSpeedEffect());

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "+1: Draw a card, then discard a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new PhaseOutEffect(PhaseOutSubject.TARGET)),
                "\u22123: Target creature you don't control phases out.",
                TargetFilters.creatureAnOpponentControls()
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new ControllerExtraTurnEffect(2)),
                "\u221210: Take two extra turns after this one."
        ));
    }
}
