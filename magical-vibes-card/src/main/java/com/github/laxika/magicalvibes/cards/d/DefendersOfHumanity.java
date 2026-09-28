package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "11")
public class DefendersOfHumanity extends Card {

    public DefendersOfHumanity() {
        CreateTokenEffect astartesWarriors = new CreateTokenEffect(
                new XValue(), "Astartes Warrior", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.ASTARTES, CardSubtype.WARRIOR),
                Set.of(Keyword.VIGILANCE), Set.of());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, astartesWarriors);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{2}{W}",
                List.of(new ExileSelfCost(), astartesWarriors),
                "{X}{2}{W}, Exile this enchantment: Create X 2/2 white Astartes Warrior creature tokens with vigilance. "
                        + "Activate only if you control no creatures and only during your turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ).withActivationCondition(
                new ControlsPermanentCountAtMost(0, new PermanentIsCreaturePredicate()),
                "Activate only if you control no creatures"));
    }
}
