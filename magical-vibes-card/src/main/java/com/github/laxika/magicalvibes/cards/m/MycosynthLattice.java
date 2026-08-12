package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllCardsAreColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorEffect;

@CardRegistration(set = "DST", collectorNumber = "130")
public class MycosynthLattice extends Card {

    public MycosynthLattice() {
        addEffect(EffectSlot.STATIC, new GrantCardTypeEffect(CardType.ARTIFACT, GrantScope.ALL_PERMANENTS));
        addEffect(EffectSlot.STATIC, new AllCardsAreColorlessEffect());
        addEffect(EffectSlot.STATIC, new SpendManaAsAnyColorEffect());
    }
}
