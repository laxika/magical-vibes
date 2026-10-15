package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "107")
public class JiwariTheEarthAflame extends Card {

    public JiwariTheEarthAflame() {
        PermanentPredicate withoutFlying = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{R}",
                List.of(new DealDamageToTargetCreatureEffect(new XValue())),
                "{X}{R}, {T}: Jiwari, the Earth Aflame deals X damage to target creature without flying.",
                new PermanentPredicateTargetFilter(withoutFlying, "Target must be a creature without flying")
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{X}{R}{R}{R}",
                List.of(new MassDamageEffect(0, true, false, withoutFlying)),
                "Channel — {X}{R}{R}{R}, Discard this card: It deals X damage to each creature without flying."
        ));
    }
}
