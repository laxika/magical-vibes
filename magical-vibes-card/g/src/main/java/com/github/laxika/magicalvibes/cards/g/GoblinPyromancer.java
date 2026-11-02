package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "206")
public class GoblinPyromancer extends Card {

    public GoblinPyromancer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new BoostAllCreaturesEffect(3, 0, new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)));
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                new DestroyAllPermanentsEffect(new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)));
    }
}
