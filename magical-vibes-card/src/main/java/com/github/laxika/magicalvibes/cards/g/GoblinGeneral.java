package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "P02", collectorNumber = "97")
public class GoblinGeneral extends Card {

    public GoblinGeneral() {
        // Whenever this creature attacks, Goblin creatures you control get +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(1, 1,
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)));
    }
}
