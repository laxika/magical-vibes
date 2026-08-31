package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "100")
public class NamelessOne extends Card {

    public NamelessOne() {
        PermanentCount wizardCount = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.WIZARD), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(wizardCount, wizardCount));
    }
}
