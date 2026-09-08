package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WWK", collectorNumber = "127")
public class LodestoneGolem extends Card {

    public LodestoneGolem() {
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.ARTIFACT)), 1, CostModificationScope.ALL));
    }
}
