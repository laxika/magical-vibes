package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "227")
public class PowerstoneShard extends Card {

    public PowerstoneShard() {
        // {T}: Add {C} for each artifact you control named Powerstone Shard.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AddManaPerControlledPermanentEffect(
                        ManaColor.COLORLESS,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentHasSameNameAsSourcePredicate()
                        )),
                        "artifacts named Powerstone Shard"
                )),
                "{T}: Add {C} for each artifact you control named Powerstone Shard."
        ));
    }
}
