package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.LimitControllerToOneMoreSpellThisTurnEffect;

@CardRegistration(set = "ELD", collectorNumber = "127")
@CardRegistration(set = "ELD", collectorNumber = "362")
public class IrencragFeat extends Card {

    public IrencragFeat() {
        addSpellEffect(new AwardManaEffect(ManaColor.RED, 7));
        addSpellEffect(new LimitControllerToOneMoreSpellThisTurnEffect());
    }
}
