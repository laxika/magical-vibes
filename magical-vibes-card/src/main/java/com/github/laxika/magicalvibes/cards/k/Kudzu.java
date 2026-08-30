package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SUM", collectorNumber = "205")
public class Kudzu extends Card {

    public Kudzu() {
        target(TargetFilters.land());
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED,
                SequenceEffect.of(
                        new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING),
                        new MayEffect(
                                new AttachSourceAuraToChosenPermanentEffect(new PermanentIsLandPredicate()),
                                "Attach Kudzu to a land?",
                                null,
                                MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER)));
    }
}
