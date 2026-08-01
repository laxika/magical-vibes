package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.LandsOfChosenTypeBecomeChosenBasicTypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PhaseOutTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "49")
public class VisionCharm extends Card {

    public VisionCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player mills four cards",
                        new MillEffect(4, MillRecipient.TARGET_PLAYER)),
                new ChooseOneEffect.ChooseOneOption(
                        "Choose a land type and a basic land type. Each land of the first chosen type becomes the second chosen type until end of turn",
                        new LandsOfChosenTypeBecomeChosenBasicTypeUntilEndOfTurnEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target artifact phases out",
                        new PhaseOutTargetPermanentEffect(),
                        TargetFilters.artifact())
        )));
    }
}
