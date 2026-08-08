package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "21")
public class ShiningShoal extends Card {

    public ShiningShoal() {
        addEffect(EffectSlot.SPELL, PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect.forX());
        addCastingOption(new AlternateHandCast(List.of(
                ExileCardsFromHandCastingCost.withManaValueX(new CardColorPredicate(CardColor.WHITE), "white"))));
    }
}
