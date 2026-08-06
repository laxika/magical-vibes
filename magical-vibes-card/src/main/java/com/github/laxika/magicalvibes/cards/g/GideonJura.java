package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TauntTarget;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "16")
public class GideonJura extends Card {

    public GideonJura() {
        // +2: During target opponent's next turn, creatures that player controls attack Gideon Jura if able.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new MustAttackNextTurnEffect(TauntTarget.SOURCE_PERMANENT)),
                "+2: During target opponent's next turn, creatures that player controls attack Gideon Jura if able.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));

        // -2: Destroy target tapped creature.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new DestroyTargetPermanentEffect()),
                "-2: Destroy target tapped creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTappedPredicate()
                        )),
                        "Target must be a tapped creature"
                )
        ));

        // 0: Until end of turn, Gideon Jura becomes a 6/6 Human Soldier creature that's still a
        // planeswalker. Prevent all damage that would be dealt to him this turn.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(
                        new AnimatePermanentsEffect(6, 6, List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of()),
                        PreventDamageEffect.allToSelf()
                ),
                "0: Until end of turn, Gideon Jura becomes a 6/6 Human Soldier creature that's still a "
                        + "planeswalker. Prevent all damage that would be dealt to him this turn."
        ));
    }
}
