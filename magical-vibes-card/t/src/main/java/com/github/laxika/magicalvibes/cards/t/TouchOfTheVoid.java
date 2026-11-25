package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "BFZ", collectorNumber = "134")
public class TouchOfTheVoid extends Card {

    public TouchOfTheVoid() {
        addSpellEffect(DealDamageEffect.toAnyTarget(new Fixed(3)).exilingDamagedCreaturesThatWouldDie());
    }
}
