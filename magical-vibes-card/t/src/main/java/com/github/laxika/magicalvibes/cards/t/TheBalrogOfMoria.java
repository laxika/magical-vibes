package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;

@CardRegistration(set = "LTC", collectorNumber = "46")
@CardRegistration(set = "LTC", collectorNumber = "129")
@CardRegistration(set = "LTC", collectorNumber = "449")
public class TheBalrogOfMoria extends Card {

    public TheBalrogOfMoria() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(TargetFilters.creatureAnOpponentControls(), 0, 99)
                .addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                        SequenceEffect.of(ExileGraveyardCardsEffect.cards(Subjects.sourceCard()),
                                new ConditionalEffect(HappenedThisWay.exiled(),
                                        new QueueReflexiveAbilityEffect(new ExileTargetPermanentEffect()))),
                        "Exile The Balrog of Moria?"));
        addCycling("{3}{R}");
        addTrigger(Triggers.youCycleThisCard(), CreateTokenEffect.ofTreasureToken(2));
    }
}
