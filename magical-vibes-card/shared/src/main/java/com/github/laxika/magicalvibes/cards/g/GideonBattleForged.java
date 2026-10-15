package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureMustAttackSourcePermanentNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

/**
 * Gideon, Battle-Forged — back face of Kytheon, Hero of Akros.
 * Legendary Planeswalker — Gideon (White).
 */
public class GideonBattleForged extends Card {

    public GideonBattleForged() {
        // +2: Up to one target creature an opponent controls attacks Gideon, Battle-Forged during
        // its controller's next turn if able.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new TargetCreatureMustAttackSourcePermanentNextTurnEffect()),
                "+2: Up to one target creature an opponent controls attacks Gideon, Battle-Forged "
                        + "during its controller's next turn if able.",
                null, +2, null, null,
                List.<TargetFilter>of(new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                        "Target must be a creature an opponent controls")),
                0, 1
        ));

        // +1: Until your next turn, target creature gains indestructible. Untap that creature.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET,
                                GrantDuration.UNTIL_YOUR_NEXT_TURN),
                        new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsCreaturePredicate())
                ),
                "+1: Until your next turn, target creature gains indestructible. Untap that creature.",
                TargetFilters.creature()
        ));

        // 0: Until end of turn, Gideon, Battle-Forged becomes a 4/4 Human Soldier creature with
        // indestructible that's still a planeswalker. Prevent all damage that would be dealt to him
        // this turn.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(
                        new AnimatePermanentsEffect(4, 4, List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                                Set.of(Keyword.INDESTRUCTIBLE)),
                        PreventDamageEffect.allToSelf()
                ),
                "0: Until end of turn, Gideon, Battle-Forged becomes a 4/4 Human Soldier creature with "
                        + "indestructible that's still a planeswalker. Prevent all damage that would be "
                        + "dealt to him this turn."
        ));
    }
}
