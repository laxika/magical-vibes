package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FirstMatchingSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1328")
@CardRegistration(set = "C18", collectorNumber = "47")
public class TuvasaTheSunlit extends Card {

    public TuvasaTheSunlit() {
        PermanentCount enchantmentsYouControl =
                new PermanentCount(new PermanentIsEnchantmentPredicate(), CountScope.CONTROLLER);
        addStaticEffect(new BoostSelfEffect(enchantmentsYouControl, enchantmentsYouControl));
        addTrigger(Triggers.youCastSpell(), new FirstMatchingSpellCastTriggerEffect(
                new CardTypePredicate(CardType.ENCHANTMENT), List.of(new DrawCardEffect())));
    }
}
