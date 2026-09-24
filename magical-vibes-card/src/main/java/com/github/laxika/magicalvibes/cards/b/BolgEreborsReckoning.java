package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOC", collectorNumber = "102")
public class BolgEreborsReckoning extends Card {

    public BolgEreborsReckoning() {
        PermanentPredicate otherGoblinOrOrc = new PermanentAllOfPredicate(List.of(
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN, CardSubtype.ORC)),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                new BoostAllOwnCreaturesEffect(2, 2, otherGoblinOrOrc));
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                new BoostAllCreaturesEffect(-1, -1,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
