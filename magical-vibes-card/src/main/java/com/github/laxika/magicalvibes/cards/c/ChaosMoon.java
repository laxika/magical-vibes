package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.TotalPermanentCountEven;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.LandsOfSubtypeAddExtraManaUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LandsOfSubtypeProduceFixedManaColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "179")
public class ChaosMoon extends Card {

    public ChaosMoon() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new ConditionalReplacementEffect(
                new TotalPermanentCountEven(),
                SequenceEffect.of(
                        new BoostAllCreaturesEffect(1, 1, new PermanentColorInPredicate(Set.of(CardColor.RED))),
                        new LandsOfSubtypeAddExtraManaUntilEndOfTurnEffect(CardSubtype.MOUNTAIN, ManaColor.RED)),
                SequenceEffect.of(
                        new BoostAllCreaturesEffect(-1, -1, new PermanentColorInPredicate(Set.of(CardColor.RED))),
                        new LandsOfSubtypeProduceFixedManaColorUntilEndOfTurnEffect(
                                CardSubtype.MOUNTAIN, ManaColor.COLORLESS))));
    }
}
