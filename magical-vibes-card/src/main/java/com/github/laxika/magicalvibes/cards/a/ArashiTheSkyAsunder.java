package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "121")
public class ArashiTheSkyAsunder extends Card {

    public ArashiTheSkyAsunder() {
        PermanentPredicate withFlying = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{G}",
                List.of(new DealDamageToTargetCreatureEffect(new XValue())),
                "{X}{G}, {T}: Arashi, the Sky Asunder deals X damage to target creature with flying.",
                new PermanentPredicateTargetFilter(withFlying, "Target must be a creature with flying")
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{X}{G}{G}",
                List.of(new MassDamageEffect(0, true, false, withFlying)),
                "Channel — {X}{G}{G}, Discard this card: It deals X damage to each creature with flying."
        ));
    }
}
