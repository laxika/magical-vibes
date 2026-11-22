package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherPermanentsSharing;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BRO", collectorNumber = "215")
@CardRegistration(set = "FIC", collectorNumber = "326")
public class LegionsToAshes extends Card {

    public LegionsToAshes() {
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addSpellEffect(new ExileTargetPermanentEffect(Subjects.allOf(
                        Subjects.target(),
                        Subjects.othersItsControllerControlsSharing(Subjects.target(),
                                OtherPermanentsSharing.SharedQuality.NAME, new PermanentIsTokenPredicate()))));
    }
}
