package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "265")
@CardRegistration(set = "DD1", collectorNumber = "7")
@CardRegistration(set = "EVG", collectorNumber = "7")
public class HeedlessOne extends Card {

    public HeedlessOne() {
        PermanentCount elvesOnBattlefield =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(elvesOnBattlefield, elvesOnBattlefield));
    }
}
