package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentManaValueEqualsControllerUnspentMana;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "120")
public class GlissaSunseeker extends Card {

    public GlissaSunseeker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ConditionalEffect(
                        new AllOf(List.of(
                                new TargetPermanentMatches(new PermanentIsArtifactPredicate()),
                                new TargetPermanentManaValueEqualsControllerUnspentMana()
                        )),
                        new DestroyTargetPermanentEffect()
                )),
                "{T}: Destroy target artifact if its mana value is equal to the amount of unspent mana you have.",
                TargetFilters.artifact()
        ));
    }
}
