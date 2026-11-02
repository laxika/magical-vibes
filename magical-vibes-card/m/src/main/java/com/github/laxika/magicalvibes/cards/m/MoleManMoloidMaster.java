package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "177")
public class MoleManMoloidMaster extends Card {

    public MoleManMoloidMaster() {
        addStaticEffect(new PlayLandsFromGraveyardEffect());

        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new CreateTokenEffect(
                CardType.CREATURE, 1, "Moloid", 1, 1,
                CardColor.GREEN, Set.of(CardColor.GREEN),
                List.of(CardSubtype.MINION), Set.of(), Set.of(),
                false, false,
                List.of(new TriggeredAbility(Triggers.attacks(),
                        new MayEffect(new MillEffect(1, MillRecipient.CONTROLLER), "Mill a card?"))),
                List.of(), false, false, false, 0, Set.of()));
    }
}
