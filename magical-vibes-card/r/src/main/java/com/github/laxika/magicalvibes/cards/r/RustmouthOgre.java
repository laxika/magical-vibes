package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "103")
public class RustmouthOgre extends Card {

    public RustmouthOgre() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(
                        new DestroyPermanentDamagedPlayerControlsEffect(new PermanentIsArtifactPredicate(), 0),
                        "You may destroy target artifact that player controls."));
    }
}
