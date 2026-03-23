package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfAsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAtEndOfCombatAndReturnTransformedEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "234")
public class ConquerorsGalleon extends Card {

    public ConquerorsGalleon() {
        // Set up back face
        ConquerorsFoothold backFace = new ConquerorsFoothold();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // When Conqueror's Galleon attacks, exile it at end of combat, then return it to the
        // battlefield transformed under your control.
        addEffect(EffectSlot.ON_ATTACK, new ExileSelfAtEndOfCombatAndReturnTransformedEffect());

        // Crew 4
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new CrewCost(4), new AnimateSelfAsCreatureEffect()),
                "Crew 4"
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ConquerorsFoothold";
    }
}
