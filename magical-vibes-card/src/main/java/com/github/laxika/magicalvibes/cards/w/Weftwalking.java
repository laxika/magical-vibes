package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleControllerHandAndGraveyardIntoLibraryEffect;

@CardRegistration(set = "EOE", collectorNumber = "86")
public class Weftwalking extends Card {

    public Weftwalking() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new WasCast(),
                SequenceEffect.of(
                        new ShuffleControllerHandAndGraveyardIntoLibraryEffect(),
                        new DrawCardEffect(7))));

        addEffect(EffectSlot.STATIC, new AlternativeCostForSpellsEffect(
                "{0}", null, null, true, false, true, false, true, null, null, null));
    }
}
