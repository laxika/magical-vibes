package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.AwardOneManaOfEachColorAmongCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

import java.util.List;

public class SunbirdEffigy extends Card {

    public SunbirdEffigy() {
        ColorsAmongCardsExiledWithSource colors = new ColorsAmongCardsExiledWithSource();
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(colors, colors));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardOneManaOfEachColorAmongCardsExiledWithSourceEffect()),
                "{T}: For each color among the exiled cards used to craft this creature, add one mana of that color."));
    }
}
