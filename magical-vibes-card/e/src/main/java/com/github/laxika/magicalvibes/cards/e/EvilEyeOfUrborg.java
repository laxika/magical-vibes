package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "107")
public class EvilEyeOfUrborg extends Card {

    public EvilEyeOfUrborg() {
        addStaticEffect(new ControlledCreaturesCantAttackUnlessPredicateEffect(
                new PermanentHasSubtypePredicate(CardSubtype.EYE)));
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, new DestroyCombatOpponentEffect(false));
    }
}
