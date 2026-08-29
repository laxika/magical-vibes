package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MMQ", collectorNumber = "234")
public class CausticWasps extends Card {

    public CausticWasps() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new DestroyPermanentDamagedPlayerControlsEffect(
                        new PermanentIsArtifactPredicate(), 0),
                        "You may destroy target artifact that player controls."));
    }
}
