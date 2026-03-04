package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "75")
public class Slagstorm extends Card {

    public Slagstorm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Slagstorm deals 3 damage to each creature", new MassDamageEffect(3)),
                new ChooseOneEffect.ChooseOneOption("Slagstorm deals 3 damage to each player", new DealDamageToEachPlayerEffect(3))
        )));
    }
}
