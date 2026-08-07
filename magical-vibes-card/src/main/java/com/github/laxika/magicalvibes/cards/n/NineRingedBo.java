package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "263")
public class NineRingedBo extends Card {

    public NineRingedBo() {
        // {T}: This artifact deals 1 damage to target Spirit creature. If that creature would die
        // this turn, exile it instead.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MarkTargetCreatureExileInsteadOfDieThisTurnEffect(),
                        new DealDamageToTargetCreatureEffect(1)),
                "{T}: This artifact deals 1 damage to target Spirit creature. If that creature would die this turn, exile it instead.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT))),
                        "Target must be a Spirit creature"
                )
        ));
    }
}
