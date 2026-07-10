package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "203")
public class DauntlessDourbark extends Card {

    public DauntlessDourbark() {
        // Power and toughness are each equal to the number of Forests you control plus the number of Treefolk you control.
        Sum forestsPlusTreefolk = new Sum(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER),
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.TREEFOLK), CountScope.CONTROLLER));
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(forestsPlusTreefolk, forestsPlusTreefolk));

        // Has trample as long as you control another Treefolk.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsAnotherPermanent(new PermanentHasSubtypePredicate(CardSubtype.TREEFOLK)),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
    }
}
