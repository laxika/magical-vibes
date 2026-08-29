package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreCardsInHandThanEachOpponent;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "15")
public class KitsuneBonesetter extends Card {

    public KitsuneBonesetter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTargetCreature(3)),
                "{T}: Prevent the next 3 damage that would be dealt to target creature this turn. "
                        + "Activate only if you have more cards in hand than each opponent.",
                TargetFilters.creature()
        ).withActivationCondition(
                new ControllerHasMoreCardsInHandThanEachOpponent(),
                "Activate only if you have more cards in hand than each opponent."));
    }
}
