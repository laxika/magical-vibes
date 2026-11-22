package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "SPG", collectorNumber = "58")
@CardRegistration(set = "C17", collectorNumber = "27")
public class KindredCharge extends Card {

    public KindredCharge() {
        addSpellEffect(new ChooseCreatureTypeAtResolutionEffect());
        addSpellEffect(new CreateTokenCopyOfTargetPermanentEffect(true, true).withSubject(Subjects.controlledBy(
                Subjects.you(), new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                        new PermanentHasTypeChosenThisResolutionPredicate())))));
    }
}
