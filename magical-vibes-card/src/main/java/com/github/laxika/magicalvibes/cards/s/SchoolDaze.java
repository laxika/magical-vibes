package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "42")
public class SchoolDaze extends Card {

    public SchoolDaze() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Do Homework — Draw three cards",
                        new DrawCardEffect(3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Fight Crime — Counter target spell. Draw a card",
                        List.of(new CounterSpellEffect(), new DrawCardEffect(1)))
        )));
    }
}
