package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceExiledCardsThreshold;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndReturnCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "254")
@CardRegistration(set = "TDC", collectorNumber = "315")
public class ColfenorsUrn extends Card {

    public ColfenorsUrn() {
        // Whenever a creature with toughness 4 or greater is put into your graveyard from the
        // battlefield, you may exile it (tracked with this artifact).
        addTrigger(Triggers.dies(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentToughnessAtLeastPredicate(4))),
                        ObjectRelation.YOU_OWN),
                new MayEffect(new ExileTriggeringCreatureAndTrackWithSourceEffect(),
                        "Exile the creature with Colfenor's Urn?"));
        // At the beginning of the end step, if three or more cards have been exiled with this
        // artifact, sacrifice it. If you do, return those cards to the battlefield under their
        // owner's control.
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceExiledCardsThreshold(3)),
                new SacrificeSelfAndReturnCardsExiledWithSourceEffect(3));
    }
}
