package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "235")
public class JanJansenChaosCrafter extends Card {

    public JanJansenChaosCrafter() {
        PermanentAllOfPredicate artifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()
        ));
        PermanentAllOfPredicate noncreatureArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(artifactCreature, "an artifact creature", false),
                        CreateTokenEffect.ofTreasureToken(2)
                ),
                "{T}, Sacrifice an artifact creature: Create two Treasure tokens."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(noncreatureArtifact, "a noncreature artifact", false),
                        new CreateTokenEffect(2, "Construct", 1, 1, null,
                                List.of(CardSubtype.CONSTRUCT), Set.of(), Set.of(CardType.ARTIFACT))
                ),
                "{T}, Sacrifice a noncreature artifact: Create two 1/1 colorless Construct artifact creature tokens."
        ));
    }
}
