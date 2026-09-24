package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "2ED", collectorNumber = "236")
public class ChaosOrb extends Card {

    public ChaosOrb() {
        PermanentAllOfPredicate nontokenPermanentsOtherThanSource = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ConditionalEffect(
                        new SourceIsOnBattlefield(),
                        new DestroyAllPermanentsEffect(
                                nontokenPermanentsOtherThanSource,
                                new DestroySourceEffect()))),
                "{1}, {T}: Destroy all nontoken permanents this artifact touches. Then destroy this artifact."
        ));
    }
}
