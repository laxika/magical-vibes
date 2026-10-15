package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "151")
public class SardianCliffstomper extends Card {

    public SardianCliffstomper() {
        var mountain = new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN);
        var mountains = new PermanentCount(mountain, CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(new ControllerTurn(), new ControlsPermanentCount(4, mountain))),
                new BoostSelfEffect(mountains, new Fixed(0))));
    }
}
