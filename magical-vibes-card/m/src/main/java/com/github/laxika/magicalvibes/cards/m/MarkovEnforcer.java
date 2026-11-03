package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOC", collectorNumber = "26")
@CardRegistration(set = "VOC", collectorNumber = "64")
public class MarkovEnforcer extends Card {

    public MarkovEnforcer() {
        // Whenever this creature or another Vampire you control enters, this creature fights up to
        // one target creature an opponent controls.
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.enters(
                                new PermanentAllOfPredicate(List.of(
                                        Triggers.CREATURE,
                                        new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE))),
                                ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                        new SourceFightsTargetCreatureEffect());

        // Whenever a creature dealt damage by this creature this turn dies, create a Blood token.
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), CreateTokenEffect.ofBloodToken(1));
    }
}
