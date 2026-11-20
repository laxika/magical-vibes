package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MH1", collectorNumber = "219")
public class AmorphousAxe extends Card {

    public AmorphousAxe() {
        addStaticEffect(new StaticBoostEffect(3, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantAllCreatureTypesToOwnCreaturesEffect(Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
