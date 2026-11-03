package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "79")
public class ValleyFloodcaller extends Card {

    private static final PermanentPredicate FLOODCALLER_CREATURES = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasAnySubtypePredicate(Set.of(
                    CardSubtype.BIRD,
                    CardSubtype.FROG,
                    CardSubtype.OTTER,
                    CardSubtype.RAT))));

    public ValleyFloodcaller() {
        addStaticEffect(new GrantFlashToCardTypeEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new BoostAllOwnCreaturesEffect(1, 1, FLOODCALLER_CREATURES),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, FLOODCALLER_CREATURES));
    }
}
