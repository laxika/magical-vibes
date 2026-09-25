package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "204")
public class ThunderclapDrake extends Card {

    public ThunderclapDrake() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )), 1, CostModificationScope.SELF));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(
                        new SacrificeSelfCost(),
                        new CopyNextInstantOrSorceryCastThisTurnEffect(
                                new CommanderCastsFromCommandZoneThisGame())
                ),
                "{2}{U}, Sacrifice this creature: When you next cast an instant or sorcery spell this turn, "
                        + "copy it for each time you've cast your commander from the command zone this game. "
                        + "You may choose new targets for the copies."
        ));
    }
}
