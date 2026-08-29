package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "35")
public class ErayoSoratamiAscendant extends Card {

    public ErayoSoratamiAscendant() {
        setBackFaceCard(new ErayosEssence());

        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new NthSpellCastTriggerEffect(4, List.of(new TransformSelfEffect()), CountScope.ANY_PLAYER));
    }

    @Override
    public String getBackFaceClassName() {
        return "ErayosEssence";
    }
}
