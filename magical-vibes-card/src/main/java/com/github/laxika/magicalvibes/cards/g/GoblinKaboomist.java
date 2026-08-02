package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "144")
public class GoblinKaboomist extends Card {

    public GoblinKaboomist() {
        // At the beginning of your upkeep, create a colorless artifact token named Land Mine with
        // "{R}, Sacrifice this token: This token deals 2 damage to target attacking creature without flying."
        // Then flip a coin. If you lose the flip, this creature deals 2 damage to itself.
        ActivatedAbility landMineAbility = new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(2)),
                "{R}, Sacrifice this token: This token deals 2 damage to target attacking creature without flying.",
                new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                )),
                        "Target must be an attacking creature without flying."
                )
        );

        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                CreateTokenEffect.ofArtifactToken(1, "Land Mine", List.of(), List.of(landMineAbility)),
                new FlipCoinWinEffect(null, new DealDamageToSourceEffect(2))
        ));
    }
}
