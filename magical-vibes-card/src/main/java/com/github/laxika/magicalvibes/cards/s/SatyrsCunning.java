package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "152")
public class SatyrsCunning extends Card {

    public SatyrsCunning() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1, "Satyr", 1, 1, CardColor.RED, List.of(CardSubtype.SATYR),
                Set.of(), Set.of(), Map.of(EffectSlot.STATIC, new CantBlockEffect())));

        addCastingOption(new GraveyardCast(null, "{2}{R}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 2))));
    }
}
