package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "223")
public class SteamVines extends Card {

    public SteamVines() {
        target(TargetFilters.land());
        addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                SequenceEffect.of(
                        new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING),
                        new DealDamageToPlayersEffect(1, Subjects.controllerOf(Subjects.triggering())),
                        new AttachSourceAuraToChosenPermanentEffect(new PermanentIsLandPredicate())));
    }
}
