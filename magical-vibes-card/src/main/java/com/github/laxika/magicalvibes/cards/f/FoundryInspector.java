package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "KLD", collectorNumber = "215")
@CardRegistration(set = "SLD", collectorNumber = "831")
@CardRegistration(set = "SLD", collectorNumber = "979")
@CardRegistration(set = "SLD", collectorNumber = "2106")
@CardRegistration(set = "KLR", collectorNumber = "241")
@CardRegistration(set = "BRR", collectorNumber = "16")
@CardRegistration(set = "SLZ", collectorNumber = "104")
@CardRegistration(set = "SLZ", collectorNumber = "225")
@CardRegistration(set = "SLZ", collectorNumber = "346")
public class FoundryInspector extends Card {

    public FoundryInspector() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.ARTIFACT), 1, CostModificationScope.SELF));
    }
}
