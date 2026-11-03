package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "148")
@CardRegistration(set = "DBL", collectorNumber = "415")
public class CemeteryGatekeeper extends Card {

    public CemeteryGatekeeper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileAnyGraveyardCardAndImprintOnSourceEffect());

        CardSharesCardTypeWithImprintedCardPredicate sharedType =
                new CardSharesCardTypeWithImprintedCardPredicate(true);
        CardEffect damage = new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER);
        CardEffect conditionalDamage = new TriggeringCardConditionalEffect(sharedType, damage);

        addTrigger(Triggers.playerCastsSpell().withFilter(sharedType), conditionalDamage);
        addTrigger(Triggers.youPlayLand().withFilter(sharedType), damage);
        addTrigger(Triggers.opponentPlaysLand().withFilter(sharedType), damage);
    }
}
