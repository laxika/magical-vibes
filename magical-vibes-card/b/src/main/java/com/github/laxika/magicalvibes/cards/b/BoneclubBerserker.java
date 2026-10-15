package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "126")
public class BoneclubBerserker extends Card {

    public BoneclubBerserker() {
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new Scaled(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN), CountScope.CONTROLLER, true), 2),
                new Fixed(0)));
    }
}
