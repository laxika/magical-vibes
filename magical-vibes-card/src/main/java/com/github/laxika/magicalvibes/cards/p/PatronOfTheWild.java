package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LGN", collectorNumber = "134")
public class PatronOfTheWild extends Card {

    public PatronOfTheWild() {
        addMorph("{2}{G}");
        target(TargetFilters.creature()).addEffect(
                EffectSlot.ON_TURNED_FACE_UP,
                new BoostTargetCreatureEffect(3, 3));
    }
}
