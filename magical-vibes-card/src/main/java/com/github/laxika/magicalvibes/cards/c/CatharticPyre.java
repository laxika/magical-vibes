package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "133")
public class CatharticPyre extends Card {

    public CatharticPyre() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Cathartic Pyre deals 3 damage to target creature or planeswalker",
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Discard up to two cards, then draw that many cards",
                        new DiscardUpToThenDrawThatManyEffect(2))
        )));
    }
}
