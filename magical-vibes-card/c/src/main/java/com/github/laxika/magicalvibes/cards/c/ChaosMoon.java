package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.TotalPermanentCountEven;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.LandsOfSubtypeAddExtraManaUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "179")
public class ChaosMoon extends Card {

    public ChaosMoon() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new ConditionalReplacementEffect(
                new TotalPermanentCountEven(),
                SequenceEffect.of(
                        BoostEffect.allCreatures(1, 1, new PermanentColorInPredicate(Set.of(CardColor.RED))),
                        new LandsOfSubtypeAddExtraManaUntilEndOfTurnEffect(CardSubtype.MOUNTAIN, ManaColor.RED)),
                SequenceEffect.of(
                        BoostEffect.allCreatures(-1, -1, new PermanentColorInPredicate(Set.of(CardColor.RED))),
                        new ReplaceLandManaWithColorEffect(ManaColor.COLORLESS)
                                .forLands(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN))
                                .lasting(Duration.UNTIL_END_OF_TURN))));
    }
}
