package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaDealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "76")
@CardRegistration(set = "SPM", collectorNumber = "260")
public class ElectroAssaultingBattery extends Card {

    public ElectroAssaultingBattery() {
        addEffect(EffectSlot.STATIC, new PreventManaDrainEffect(ManaColor.RED));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                List.of(new AwardManaEffect(ManaColor.RED))));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new PayXManaDealXDamageToAnyTargetEffect("{X}", TargetPredicates.player()));
    }
}
