package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "30")
@CardRegistration(set = "OTC", collectorNumber = "66")
public class SmolderingStagecoach extends Card {

    public SmolderingStagecoach() {
        CardsInGraveyard instantsAndSorceries = new CardsInGraveyard(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(instantsAndSorceries, new Fixed(5)));

        addEffect(EffectSlot.ON_ATTACK, new RegisterDelayedControllerSpellCastTriggerEffect(
                new CardTypePredicate(CardType.INSTANT),
                List.of(new CascadeEffect()),
                true,
                false));
        addEffect(EffectSlot.ON_ATTACK, new RegisterDelayedControllerSpellCastTriggerEffect(
                new CardTypePredicate(CardType.SORCERY),
                List.of(new CascadeEffect()),
                true,
                false));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
