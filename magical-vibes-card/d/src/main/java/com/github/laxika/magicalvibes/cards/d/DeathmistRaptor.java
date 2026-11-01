package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldFaceDownEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "180")
@CardRegistration(set = "DSC", collectorNumber = "176")
@CardRegistration(set = "MKC", collectorNumber = "168")
@CardRegistration(set = "C19", collectorNumber = "160")
public class DeathmistRaptor extends Card {

    public DeathmistRaptor() {
        addMorph("{4}{G}");
        addStaticEffect(new PutCountersOnTurnFaceUpEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, false));
        addTrigger(new TriggerCondition(Triggers.turnedFaceUp(null,
                ObjectRelation.YOU_CONTROL), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Face up",
                                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false)),
                        new ChooseOneEffect.ChooseOneOption("Face down",
                                new ReturnSourceCardFromGraveyardToBattlefieldFaceDownEffect())
                )),
                "Return Deathmist Raptor to the battlefield?"));
    }
}
