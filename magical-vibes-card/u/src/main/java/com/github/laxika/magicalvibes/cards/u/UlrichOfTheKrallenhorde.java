package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "191")
@CardRegistration(set = "SIR", collectorNumber = "246")
public class UlrichOfTheKrallenhorde extends Card {

    public UlrichOfTheKrallenhorde() {
        setBackFaceCard(new UlrichUncontestedAlpha());

        target(TargetFilters.creature());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostTargetCreatureEffect(4, 4));
        addTrigger(Triggers.transformsToFrontFace(ObjectRelation.THIS), new BoostTargetCreatureEffect(4, 4));

        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new NoSpellsCastLastTurn()),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "UlrichUncontestedAlpha";
    }
}
