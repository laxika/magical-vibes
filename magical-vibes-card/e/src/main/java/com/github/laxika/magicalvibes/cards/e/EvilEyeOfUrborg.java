package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TSP", collectorNumber = "107")
public class EvilEyeOfUrborg extends Card {

    public EvilEyeOfUrborg() {
        addEffect(EffectSlot.STATIC, new ControlledCreaturesCantAttackUnlessPredicateEffect(
                new PermanentHasSubtypePredicate(CardSubtype.EYE)));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DestroyCombatOpponentEffect(false),
                TriggerMode.PER_BLOCKER);
    }
}
