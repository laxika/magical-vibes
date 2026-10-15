package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerCastTwoOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "117")
public class ReverberatingSummons extends Card {

    public ReverberatingSummons() {
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(
                        new ControllerCastTwoOrMoreSpellsThisTurn(new CardTruePredicate()),
                        new AnimatePermanentsEffect(3, 3, List.of(CardSubtype.MONK), Set.of(Keyword.HASTE))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new DiscardHandCost(), new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{1}{R}, Discard your hand, Sacrifice this enchantment: Draw two cards."
        ));
    }
}
