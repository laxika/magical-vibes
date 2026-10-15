package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "19")
public class RuinGhost extends Card {

    public RuinGhost() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(FlickerEffect.flickerTargetUnderYourControl()),
                "{W}, {T}: Exile target land you control, then return it to the battlefield under your control.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentIsLandPredicate()
                        )),
                        "Target must be a land you control"
                )
        ));
    }
}
