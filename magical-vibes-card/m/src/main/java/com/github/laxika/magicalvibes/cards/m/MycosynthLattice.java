package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AllCardsAreColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorEffect;

@CardRegistration(set = "DST", collectorNumber = "130")
@CardRegistration(set = "SLD", collectorNumber = "1434")
public class MycosynthLattice extends Card {

    public MycosynthLattice() {
        addStaticEffect(new GrantCardTypeEffect(CardType.ARTIFACT, GrantScope.ALL_PERMANENTS));
        addStaticEffect(new AllCardsAreColorlessEffect());
        addStaticEffect(new SpendManaAsAnyColorEffect());
    }
}
