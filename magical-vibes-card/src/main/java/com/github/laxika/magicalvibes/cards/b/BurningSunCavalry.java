package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LCI", collectorNumber = "138")
public class BurningSunCavalry extends Card {

    public BurningSunCavalry() {
        ControlsPermanent controlsDinosaur = new ControlsPermanent(
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR));
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(controlsDinosaur, new BoostSelfEffect(1, 1)));
        addEffect(EffectSlot.ON_BLOCK, new ConditionalEffect(controlsDinosaur, new BoostSelfEffect(1, 1)));
    }
}
