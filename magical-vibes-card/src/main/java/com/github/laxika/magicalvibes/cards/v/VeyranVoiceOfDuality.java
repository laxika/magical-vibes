package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1915")
@CardRegistration(set = "SOC", collectorNumber = "337")
public class VeyranVoiceOfDuality extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public VeyranVoiceOfDuality() {
        List<CardEffect> boost = List.of(new BoostSelfEffect(1, 1));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(INSTANT_OR_SORCERY, boost));
        addEffect(EffectSlot.ON_CONTROLLER_COPIES_SPELL,
                new SpellCopyTriggerEffect(INSTANT_OR_SORCERY, boost));
        addEffect(EffectSlot.STATIC, AdditionalTriggeredAbilityEffect.forInstantOrSorceryCastOrCopy());
    }
}
