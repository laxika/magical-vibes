package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCountersOnTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "187")
@CardRegistration(set = "SCD", collectorNumber = "183")
public class FoeRazerRegent extends Card {

    public FoeRazerRegent() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                        new SourceFightsTargetCreatureEffect(),
                        "Have this creature fight target creature you don't control?"));
        addTrigger(Triggers.creatureYouControlFights(),
                new RegisterDelayedCountersOnTriggeringPermanentEffect(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)));
    }
}
