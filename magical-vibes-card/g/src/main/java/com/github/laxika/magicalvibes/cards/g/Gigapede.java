package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "264")
@CardRegistration(set = "VMA", collectorNumber = "214")
public class Gigapede extends Card {

    public Gigapede() {
        addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()),
                new MayEffect(
                        SequenceEffect.of(
                                new DiscardEffect(1, Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.discarded(),
                                        new PutCardsIntoHandEffect(Subjects.sourceCard()))),
                        "Discard a card to return Gigapede from your graveyard to your hand?"));
    }
}
