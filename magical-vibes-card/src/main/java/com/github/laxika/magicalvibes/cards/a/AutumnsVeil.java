package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantControllerCreaturesCantBeTargetedByColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerSpellsCantBeCounteredByColorsEffect;

import java.util.Set;

@CardRegistration(set = "M11", collectorNumber = "162")
public class AutumnsVeil extends Card {

    public AutumnsVeil() {
        Set<CardColor> colors = Set.of(CardColor.BLUE, CardColor.BLACK);
        addEffect(EffectSlot.SPELL, new GrantControllerSpellsCantBeCounteredByColorsEffect(colors));
        addEffect(EffectSlot.SPELL, new GrantControllerCreaturesCantBeTargetedByColorsEffect(colors));
    }
}
