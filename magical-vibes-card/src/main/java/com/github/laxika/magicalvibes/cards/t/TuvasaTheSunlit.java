package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1328")
public class TuvasaTheSunlit extends Card {

    public TuvasaTheSunlit() {
        PermanentCount enchantmentsYouControl =
                new PermanentCount(new PermanentIsEnchantmentPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(enchantmentsYouControl, enchantmentsYouControl));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new OncePerTurnTriggerEffect(
                new SpellCastTriggerEffect(new CardTypePredicate(CardType.ENCHANTMENT),
                        List.of(new DrawCardEffect()))));
    }
}
