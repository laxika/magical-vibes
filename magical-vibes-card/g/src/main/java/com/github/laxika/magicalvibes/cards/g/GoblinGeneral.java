package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "P02", collectorNumber = "97")
@CardRegistration(set = "S99", collectorNumber = "101")
@CardRegistration(set = "VMA", collectorNumber = "165")
@CardRegistration(set = "ME4", collectorNumber = "121")
public class GoblinGeneral extends Card {

    public GoblinGeneral() {
        // Whenever this creature attacks, Goblin creatures you control get +1/+1 until end of turn.
        addTrigger(Triggers.attacks(), new BoostAllOwnCreaturesEffect(1, 1,
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)));
    }
}
