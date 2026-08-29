package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

public class WestvaleCultLeader extends Card {

    public WestvaleCultLeader() {
        PermanentCount creaturesYouControl =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(creaturesYouControl, creaturesYouControl));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new CreateTokenEffect(1, "Human Cleric", 1, 1,
                        CardColor.WHITE, Set.of(CardColor.WHITE, CardColor.BLACK),
                        List.of(CardSubtype.HUMAN, CardSubtype.CLERIC)));
    }
}
