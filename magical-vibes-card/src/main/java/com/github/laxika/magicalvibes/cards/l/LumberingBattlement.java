package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnNontokenCreaturesUntilSourceLeavesEffect;

@CardRegistration(set = "RNA", collectorNumber = "15")
public class LumberingBattlement extends Card {

    public LumberingBattlement() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileOwnNontokenCreaturesUntilSourceLeavesEffect());
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new Scaled(new CardsExiledWithSource(), 2),
                new Scaled(new CardsExiledWithSource(), 2)));
    }
}
