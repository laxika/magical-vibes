package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "FDN", collectorNumber = "130")
public class QuickDrawKatana extends Card {

    public QuickDrawKatana() {
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new StaticBoostEffect(2, 0, Subjects.attachedCreature())));
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.attachedCreature())));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
