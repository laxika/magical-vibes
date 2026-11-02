package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "45")
@CardRegistration(set = "FDN", collectorNumber = "309")
@CardRegistration(set = "FDN", collectorNumber = "377")
@CardRegistration(set = "FDN", collectorNumber = "455")
public class KioraTheRisingTide extends Card {

    public KioraTheRisingTide() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new DrawCardEffect(2),
                new DiscardEffect(2, DiscardRecipient.CONTROLLER)));

        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new MayEffect(new CreateTokenEffect(
                        CardType.CREATURE, 1, "Scion of the Deep", 8, 8,
                        CardColor.BLUE, null, List.of(CardSubtype.OCTOPUS),
                        Set.of(), Set.of(), false, false, List.of(), List.of(),
                        false, false, true, 0, Set.of()),
                        "Create Scion of the Deep?")));
    }
}
