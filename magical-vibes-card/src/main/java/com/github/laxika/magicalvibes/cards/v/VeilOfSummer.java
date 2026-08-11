package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerAndPermanentsHexproofFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerSpellsCantBeCounteredThisTurnEffect;
import com.github.laxika.magicalvibes.model.condition.OpponentCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "198")
public class VeilOfSummer extends Card {

    public VeilOfSummer() {
        Set<CardColor> colors = Set.of(CardColor.BLUE, CardColor.BLACK);
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new OpponentCastSpellThisTurn(new CardAnyOfPredicate(List.of(
                        new CardColorPredicate(CardColor.BLUE),
                        new CardColorPredicate(CardColor.BLACK)
                ))),
                new DrawCardEffect()));
        addEffect(EffectSlot.SPELL, new GrantControllerSpellsCantBeCounteredThisTurnEffect());
        addEffect(EffectSlot.SPELL, new GrantControllerAndPermanentsHexproofFromColorsEffect(colors));
    }
}
