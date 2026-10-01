package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasCascadePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "128")
@CardRegistration(set = "WHO", collectorNumber = "414")
@CardRegistration(set = "WHO", collectorNumber = "552")
@CardRegistration(set = "WHO", collectorNumber = "733")
@CardRegistration(set = "WHO", collectorNumber = "1005")
@CardRegistration(set = "WHO", collectorNumber = "1143")
public class TheFirstDoctor extends Card {

    public TheFirstDoctor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryAndOrGraveyardForCardToHandEffect(new CardNamedPredicate("TARDIS")));

        PermanentAnyOfPredicate artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardHasCascadePredicate(),
                        List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        null,
                        new PermanentPredicateTargetFilter(
                                artifactOrCreature,
                                "Target must be an artifact or creature")));
    }
}
