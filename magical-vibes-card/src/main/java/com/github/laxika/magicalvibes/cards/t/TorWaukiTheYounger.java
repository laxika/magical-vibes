package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "46")
@CardRegistration(set = "DMC", collectorNumber = "68")
public class TorWaukiTheYounger extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public TorWaukiTheYounger() {
        addEffect(EffectSlot.STATIC, new AdditionalControllerDamageEffect(1, null, true));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                INSTANT_OR_SORCERY,
                List.of(new DealDamageToAnyTargetEffect(2))));
    }
}
