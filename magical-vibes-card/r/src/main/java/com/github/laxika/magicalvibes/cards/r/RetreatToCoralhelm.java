package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "82")
@CardRegistration(set = "SLD", collectorNumber = "407")
@CardRegistration(set = "DSC", collectorNumber = "126")
public class RetreatToCoralhelm extends Card {

    public RetreatToCoralhelm() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "You may tap or untap target creature.",
                        new MayEffect(
                                new TapOrUntapTargetPermanentEffect(new PermanentIsCreaturePredicate()),
                                "Tap or untap target creature?"),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption("Scry 1.", new ScryEffect(1))
        )));
    }
}
