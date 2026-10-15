package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeToReduceColoredCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "16")
public class DefilerOfFaith extends Card {

    private static CardAllOfPredicate whitePermanentSpell() {
        return new CardAllOfPredicate(List.of(
                new CardColorPredicate(CardColor.WHITE),
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))
        ));
    }

    public DefilerOfFaith() {
        CardAllOfPredicate whitePermanentFilter = whitePermanentSpell();
        addEffect(EffectSlot.STATIC, new PayLifeToReduceColoredCastCostEffect(
                whitePermanentFilter, "{W}", 2, CostModificationScope.SELF));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                whitePermanentFilter, List.of(CreateTokenEffect.whiteSoldier(1))));
    }
}
