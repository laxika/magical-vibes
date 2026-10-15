package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsUnblockedAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "124")
public class Snag extends Card {

    public Snag() {
        addCastingOption(new AlternateHandCast(List.of(
                new DiscardCardCastingCost(new CardSubtypePredicate(CardSubtype.FOREST), "a Forest card"))));
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatExcept(
                new PermanentNotPredicate(new PermanentIsUnblockedAttackingPredicate())));
    }
}
