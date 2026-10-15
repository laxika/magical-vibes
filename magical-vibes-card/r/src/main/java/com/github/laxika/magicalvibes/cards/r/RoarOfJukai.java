package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockedPredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "140")
public class RoarOfJukai extends Card {

    public RoarOfJukai() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                new BoostAllCreaturesEffect(2, 2, new PermanentIsBlockedPredicate())));
        addEffect(EffectSlot.STATIC, new SpliceEffect(CardSubtype.ARCANE, List.of(),
                List.of(new GainLifeEffect(new Fixed(5), GainLifeRecipient.OPPONENT))));
    }
}
