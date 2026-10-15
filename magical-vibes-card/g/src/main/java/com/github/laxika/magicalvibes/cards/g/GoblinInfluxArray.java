package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.ConjureGoblinInfluxArraySpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceColoredCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "YDMU", collectorNumber = "13")
public class GoblinInfluxArray extends Card {

    public GoblinInfluxArray() {
        addEffect(EffectSlot.STATIC, new ReduceColoredCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.GOBLIN),
                new ManaCost("{R}"),
                CostModificationScope.SELF,
                true));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConjureGoblinInfluxArraySpellbookEffect());
    }
}
