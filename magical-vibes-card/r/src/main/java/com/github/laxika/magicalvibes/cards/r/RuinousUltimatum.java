package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "204")
@CardRegistration(set = "SPG", collectorNumber = "108")
@CardRegistration(set = "PIP", collectorNumber = "220")
@CardRegistration(set = "PIP", collectorNumber = "478")
@CardRegistration(set = "PIP", collectorNumber = "748")
@CardRegistration(set = "PIP", collectorNumber = "1006")
@CardRegistration(set = "FIC", collectorNumber = "329")
public class RuinousUltimatum extends Card {

    public RuinousUltimatum() {
        addSpellEffect(DestroyTargetPermanentEffect.all(new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ))));
    }
}
