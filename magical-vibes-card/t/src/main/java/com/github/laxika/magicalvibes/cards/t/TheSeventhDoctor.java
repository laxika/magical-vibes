package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MasterOfPredicamentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "158")
@CardRegistration(set = "WHO", collectorNumber = "442")
@CardRegistration(set = "WHO", collectorNumber = "558")
@CardRegistration(set = "WHO", collectorNumber = "763")
@CardRegistration(set = "WHO", collectorNumber = "1033")
@CardRegistration(set = "WHO", collectorNumber = "1149")
public class TheSeventhDoctor extends Card {

    public TheSeventhDoctor() {
        addTrigger(Triggers.attacks(), new MasterOfPredicamentsEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER),
                Subjects.defendingPlayer(),
                CreateTokenEffect.ofClueToken(1)));
    }
}
