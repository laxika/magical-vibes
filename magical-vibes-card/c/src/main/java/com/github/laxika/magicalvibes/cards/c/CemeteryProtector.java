package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.TriggeringCardMatches;

@CardRegistration(set = "VOW", collectorNumber = "6")
@CardRegistration(set = "DBL", collectorNumber = "273")
public class CemeteryProtector extends Card {

    public CemeteryProtector() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileAnyGraveyardCardAndImprintOnSourceEffect());

        CardSharesCardTypeWithImprintedCardPredicate sharedType =
                new CardSharesCardTypeWithImprintedCardPredicate(true);
        CardEffect createHuman = new CreateTokenEffect("Human", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN), Set.of(), Set.of());
        CardEffect conditionalHuman = new ConditionalEffect(new TriggeringCardMatches(sharedType), createHuman);

        addTrigger(Triggers.youCastSpell().withFilter(sharedType), conditionalHuman);
        addTrigger(Triggers.youPlayLand().withFilter(sharedType), conditionalHuman);
    }
}
