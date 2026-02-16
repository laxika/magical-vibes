package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderControlsLandTypeEffect;

public class SeaMonster extends Card {

    public SeaMonster() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessDefenderControlsLandTypeEffect(CardSubtype.ISLAND));
    }
}
