package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "35")
public class ErayoSoratamiAscendant extends Card {

    public ErayoSoratamiAscendant() {
        setBackFaceCard(new ErayosEssence());

        // Flipping is not transforming (CR 701.27a, CR 710), so the flip uses the flip form of
        // TransformToBackFaceEffect: no transform triggers fire and transform prevention does not apply.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new NthSpellCastTriggerEffect(4, List.of(new TransformToBackFaceEffect(true)), CountScope.ANY_PLAYER));
    }

    @Override
    public String getBackFaceClassName() {
        return "ErayosEssence";
    }
}
