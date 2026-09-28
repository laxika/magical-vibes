package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoControllerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import java.util.Map;

@CardRegistration(set = "YBLB", collectorNumber = "11")
public class SanguineSoothsayer extends Card {

    public SanguineSoothsayer() {
        addEffect(EffectSlot.ON_ATTACK, new ConjureCardIntoControllerLibraryEffect(
                "WOT", "35", Map.of(
                        EffectSlot.STATIC, List.of(
                                AlternativeCostForSpellsEffect.perpetualZeroCostForThisSpell()),
                        EffectSlot.ON_ENTER_BATTLEFIELD, List.of(new DrawCardEffect(1)))));
    }
}
