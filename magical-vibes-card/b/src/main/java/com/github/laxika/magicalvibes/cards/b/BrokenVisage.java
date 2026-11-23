package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "149")
@CardRegistration(set = "HML", collectorNumber = "43")
@CardRegistration(set = "ME2", collectorNumber = "81")
public class BrokenVisage extends Card {

    public BrokenVisage() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate())
                )),
                "Target must be a nonartifact attacking creature"
        )).addSpellEffect(new DestroyTargetPermanentEffect(true))
                .addSpellEffect(new CreateTokenEffect(CardType.CREATURE, new Fixed(1), "Spirit",
                        new TargetPower(true), new TargetToughness(), CardColor.BLACK, null,
                        List.of(CardSubtype.SPIRIT), Set.of(), Set.of(), false, false, List.of(), List.of(),
                        false, false, false, 0, Set.of()))
                .addSpellEffect(new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
