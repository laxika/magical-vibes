package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "72")
public class Spellgyre extends Card {

    public Spellgyre() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell",
                        new CounterSpellEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Surveil 2, then draw two cards",
                        List.of(new SurveilEffect(2), new DrawCardEffect(2)))
        )));
    }
}
