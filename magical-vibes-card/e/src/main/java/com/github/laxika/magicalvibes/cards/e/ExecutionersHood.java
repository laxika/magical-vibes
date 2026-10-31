package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DKA", collectorNumber = "148")
public class ExecutionersHood extends Card {

    public ExecutionersHood() {
        addStaticEffect(new GrantKeywordEffect(Keyword.INTIMIDATE, GrantScope.EQUIPPED_CREATURE));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
