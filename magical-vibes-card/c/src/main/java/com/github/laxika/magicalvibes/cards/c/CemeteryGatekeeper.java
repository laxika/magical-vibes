package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TriggeringCardMatches;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "148")
@CardRegistration(set = "DBL", collectorNumber = "415")
public class CemeteryGatekeeper extends Card {

    public CemeteryGatekeeper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileUpToOneCardFromGraveyardEffect(true).withSource());

        CardSharesCardTypeWithImprintedCardPredicate sharedType =
                new CardSharesCardTypeWithImprintedCardPredicate(true);
        CardEffect damage = DealDamageEffect.to(Subjects.triggeringPlayer(), 2);
        CardEffect conditionalDamage = new ConditionalEffect(new TriggeringCardMatches(sharedType), damage);

        addTrigger(Triggers.playerCastsSpell().withFilter(sharedType), conditionalDamage);
        addTrigger(Triggers.youPlayLand().withFilter(sharedType), conditionalDamage);
        addTrigger(Triggers.opponentPlaysLand().withFilter(sharedType), conditionalDamage);
    }
}
