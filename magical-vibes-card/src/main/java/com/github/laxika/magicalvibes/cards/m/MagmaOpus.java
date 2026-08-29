package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "203")
public class MagmaOpus extends Card {

    public MagmaOpus() {
        setAllowSharedTargets(true);

        target(1, 4).addEffect(EffectSlot.SPELL,
                DealDividedDamageEffect.chosenAmongAnyTargets(4));
        target(TargetFilters.permanent(), 2, 2)
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1, "Elemental", 4, 4, CardColor.BLUE,
                Set.of(CardColor.BLUE, CardColor.RED), List.of(CardSubtype.ELEMENTAL)));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{U/R}{U/R}",
                List.of(CreateTokenEffect.ofTreasureToken(1)),
                "{U/R}{U/R}, Discard this card: Create a Treasure token."));
    }
}
