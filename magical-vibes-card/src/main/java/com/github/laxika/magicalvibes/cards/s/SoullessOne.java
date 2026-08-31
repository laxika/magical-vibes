package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "171")
public class SoullessOne extends Card {

    public SoullessOne() {
        DynamicAmount zombieCount = new Sum(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), CountScope.ANY_PLAYER),
                new CardsInGraveyard(new CardSubtypePredicate(CardSubtype.ZOMBIE), CountScope.ANY_PLAYER));
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(zombieCount, zombieCount));
    }
}
