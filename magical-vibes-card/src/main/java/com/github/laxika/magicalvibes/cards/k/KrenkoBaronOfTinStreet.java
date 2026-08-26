package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "135")
public class KrenkoBaronOfTinStreet extends Card {

    public KrenkoBaronOfTinStreet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE,
                                1,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)
                                )))
                ),
                "{T}, Sacrifice an artifact: Put a +1/+1 counter on each Goblin you control."
        ));

        addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new MayPayManaEffect(
                        "{R}",
                        new CreateTokenEffect(
                                1,
                                "Goblin",
                                1,
                                1,
                                CardColor.RED,
                                null,
                                List.of(CardSubtype.GOBLIN),
                                Set.of(),
                                Set.of(Keyword.HASTE)),
                        "Pay {R} to create a 1/1 red Goblin creature token?"));
    }
}
