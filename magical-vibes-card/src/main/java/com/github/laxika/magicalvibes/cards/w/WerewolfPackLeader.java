package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseSubtypesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "211")
public class WerewolfPackLeader extends Card {

    public WerewolfPackLeader() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new AttackingCreaturesTotalPowerAtLeast(6),
                new DrawCardEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(
                        new SetBasePowerToughnessEffect(5, 3, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF),
                        new LoseSubtypesUntilEndOfTurnEffect(Set.of(CardSubtype.HUMAN))
                ),
                "{3}{G}: Until end of turn, this creature has base power and toughness 5/3, gains trample, and isn't a Human."
        ));
    }
}
