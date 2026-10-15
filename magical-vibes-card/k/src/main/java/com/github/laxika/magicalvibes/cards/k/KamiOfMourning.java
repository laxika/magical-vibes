package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardManaValueGreaterThanSourceManaValuePredicate;

@CardRegistration(set = "YNEO", collectorNumber = "14")
public class KamiOfMourning extends Card {

    public KamiOfMourning() {
        target(1, 1).addEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect(
                        EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_DIES,
                        new TriggeringCardConditionalEffect(
                                new CardManaValueGreaterThanSourceManaValuePredicate(),
                                new ReturnSourceCardFromGraveyardToBattlefieldEffect(true))));
    }
}
