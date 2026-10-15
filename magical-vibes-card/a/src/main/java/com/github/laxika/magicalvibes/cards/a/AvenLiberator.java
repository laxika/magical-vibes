package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SCG", collectorNumber = "4")
public class AvenLiberator extends Card {

    public AvenLiberator() {
        addMorph("{3}{W}");
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_TURNED_FACE_UP, new GrantProtectionChoiceUntilEndOfTurnEffect());
    }
}
