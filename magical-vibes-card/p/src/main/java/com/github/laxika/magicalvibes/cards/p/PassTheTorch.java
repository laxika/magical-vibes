package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandToPerpetuallyGrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RemovePerpetualTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YSNC", collectorNumber = "11")
public class PassTheTorch extends Card {

    public PassTheTorch() {
        CardEffect combatDamageAbility = new CastTargetInstantOrSorceryFromGraveyardEffect(
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                true,
                false,
                new CardNamedPredicate("Pass the Torch"),
                new RemovePerpetualTriggeredAbilityEffect(
                        Triggers.dealsCombatDamageToPlayer(),
                        CastTargetInstantOrSorceryFromGraveyardEffect.class));
        addSpellEffect(new DealDamageToAnyTargetEffect(2));
        addSpellEffect(new ChooseCardFromHandToPerpetuallyGrantTriggeredAbilityEffect(
                Triggers.dealsCombatDamageToPlayer(),
                List.of(combatDamageAbility),
                new CardTypePredicate(CardType.CREATURE)));
    }
}
