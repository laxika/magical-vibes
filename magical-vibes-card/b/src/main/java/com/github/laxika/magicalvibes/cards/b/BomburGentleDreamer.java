package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasEnduringStory;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.StoriedEffect;

@CardRegistration(set = "HOB", collectorNumber = "88")
public class BomburGentleDreamer extends Card {

    public BomburGentleDreamer() {
        addEffect(EffectSlot.STATIC, new StoriedEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new ControllerHasEnduringStory()),
                DoesntUntapEffect.self()));
    }
}
