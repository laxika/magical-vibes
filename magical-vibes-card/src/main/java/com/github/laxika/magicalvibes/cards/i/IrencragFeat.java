package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.LimitControllerToOneMoreSpellThisTurnEffect;

@CardRegistration(set = "ELD", collectorNumber = "127")
public class IrencragFeat extends Card {

    public IrencragFeat() {
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.RED, 7));
        addEffect(EffectSlot.SPELL, new LimitControllerToOneMoreSpellThisTurnEffect());
    }
}
