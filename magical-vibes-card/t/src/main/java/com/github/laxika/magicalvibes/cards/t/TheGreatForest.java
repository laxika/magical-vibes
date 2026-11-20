package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "14")
@CardRegistration(set = "OPCA", collectorNumber = "32")
@CardRegistration(set = "MOC", collectorNumber = "144")
public class TheGreatForest extends Card {

    public TheGreatForest() {
        addStaticEffect(new AssignCombatDamageWithToughnessEffect(Subjects.otherCreatures()));
        addTrigger(Triggers.chaosEnsues(), SequenceEffect.of(
                new BoostAllOwnCreaturesEffect(0, 2),
                new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.creaturesYouControl())));
    }
}
