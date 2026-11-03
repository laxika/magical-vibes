package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttackingEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "11")
@CardRegistration(set = "TDC", collectorNumber = "51")
public class AinokStrikeLeader extends Card {

    public AinokStrikeLeader() {
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AnyOf(List.of(
                                        new HasAttacker(new PermanentIsSourcePermanentPredicate()),
                                        new HasAttacker(new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCommanderPredicate(),
                                                new PermanentOwnedBySourceControllerPredicate())))
                                ))),
                new CreateTokensAttackingEachOpponentEffect(
                        new CreateTokenEffect(1, "Goblin", 1, 1, CardColor.RED,
                                List.of(CardSubtype.GOBLIN), true)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ALL_OWN_CREATURES,
                                new PermanentIsTokenPredicate())
                ),
                "Sacrifice this creature: Creature tokens you control gain indestructible until end of turn."
        ));
    }
}
