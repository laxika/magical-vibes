package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "106")
public class SeaMonster extends Card {

    public SeaMonster() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessDefenderControlsMatchingPermanentEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND),
                "an Island"
        ));
    }
}
