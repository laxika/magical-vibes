package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1915")
@CardRegistration(set = "SOC", collectorNumber = "337")
@CardRegistration(set = "C21", collectorNumber = "3")
@CardRegistration(set = "TDC", collectorNumber = "310")
@CardRegistration(set = "OTC", collectorNumber = "248")
public class VeyranVoiceOfDuality extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public VeyranVoiceOfDuality() {
        List<CardEffect> boost = List.of(new BoostSelfEffect(1, 1));
        addTrigger(Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY), boost.toArray(CardEffect[]::new));
        addTrigger(Triggers.youCopySpell().withFilter(INSTANT_OR_SORCERY), boost.toArray(CardEffect[]::new));
        addStaticEffect(AdditionalTriggeredAbilityEffect.forInstantOrSorceryCastOrCopy());
    }
}
