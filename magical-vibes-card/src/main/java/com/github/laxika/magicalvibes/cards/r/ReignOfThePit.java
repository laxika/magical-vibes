package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesCreatureCreateTokenEqualToTotalPowerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VMA", collectorNumber = "138")
public class ReignOfThePit extends Card {

    public ReignOfThePit() {
        addEffect(EffectSlot.SPELL,
                new EachPlayerSacrificesCreatureCreateTokenEqualToTotalPowerEffect(
                        new CreateTokenEffect("Demon", 0, 0, CardColor.BLACK,
                                List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), Set.of())));
    }
}
