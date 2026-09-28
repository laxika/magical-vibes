package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfCasualtyPaidEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachOpponentAndMayCastForLifeThisTurnEffect;

@CardRegistration(set = "NCC", collectorNumber = "43")
@CardRegistration(set = "NCC", collectorNumber = "144")
public class XandersPact extends Card {

    public XandersPact() {
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellIfCasualtyPaidEffect());
        addEffect(EffectSlot.SPELL, new CasualtyCost(2));
        addEffect(EffectSlot.SPELL, new ExileTopCardOfEachOpponentAndMayCastForLifeThisTurnEffect());
    }
}
