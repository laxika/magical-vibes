package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

@CardRegistration(set = "RNA", collectorNumber = "172")
public class FirebladeArtist extends Card {

    public FirebladeArtist() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentIsCreaturePredicate(),
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(2, PlayerRelation.OPPONENT),
                        "a creature"),
                "Sacrifice a creature?"));
    }
}
