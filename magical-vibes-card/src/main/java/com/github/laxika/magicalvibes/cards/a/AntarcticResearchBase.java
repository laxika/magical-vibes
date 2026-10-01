package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "567")
public class AntarcticResearchBase extends Card {

    public AntarcticResearchBase() {
        CreateTokenEffect clue = CreateTokenEffect.ofClueToken(1);
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, clue);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, clue);

        PermanentCount artifactsYouControl = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.CHAOS_TRIGGERED,
                SequenceEffect.of(
                        new PutCounterOnTargetPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE,
                                artifactsYouControl),
                        new GrantSubtypeEffect(CardSubtype.PLANT, GrantScope.TARGET)));
    }
}
