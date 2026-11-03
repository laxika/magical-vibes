package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "FIC", collectorNumber = "37")
@CardRegistration(set = "FIC", collectorNumber = "142")
public class HraesvelgrOfTheFirstBrood extends Card {

    public HraesvelgrOfTheFirstBrood() {
        // Both abilities target a creature: one target group, each ability choosing its own target.
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostTargetCreatureEffect(1, 0))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new MakeCreatureUnblockableEffect())
                .addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                        new BoostTargetCreatureEffect(1, 0),
                        new MakeCreatureUnblockableEffect());
    }
}
