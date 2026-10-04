package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "143")
@CardRegistration(set = "DDO", collectorNumber = "59")
@CardRegistration(set = "SLC", collectorNumber = "20")
@CardRegistration(set = "SLC", collectorNumber = "47")
@CardRegistration(set = "SOC", collectorNumber = "416")
@CardRegistration(set = "C13", collectorNumber = "327")
@CardRegistration(set = "CMD", collectorNumber = "290")
@CardRegistration(set = "C14", collectorNumber = "314")
@CardRegistration(set = "C15", collectorNumber = "313")
@CardRegistration(set = "WHO", collectorNumber = "320")
@CardRegistration(set = "WHO", collectorNumber = "911")
@CardRegistration(set = "PIP", collectorNumber = "311")
@CardRegistration(set = "PIP", collectorNumber = "839")
@CardRegistration(set = "C21", collectorNumber = "326")
@CardRegistration(set = "40K", collectorNumber = "300")
@CardRegistration(set = "DSC", collectorNumber = "313")
@CardRegistration(set = "MKC", collectorNumber = "305")
@CardRegistration(set = "C20", collectorNumber = "319")
@CardRegistration(set = "MIC", collectorNumber = "186")
@CardRegistration(set = "C19", collectorNumber = "280")
@CardRegistration(set = "ONC", collectorNumber = "172")
@CardRegistration(set = "WOC", collectorNumber = "172")
@CardRegistration(set = "C18", collectorNumber = "285")
@CardRegistration(set = "C17", collectorNumber = "284")
@CardRegistration(set = "FIC", collectorNumber = "438")
@CardRegistration(set = "C16", collectorNumber = "331")
@CardRegistration(set = "VOC", collectorNumber = "187")
@CardRegistration(set = "FDC", collectorNumber = "315")
public class TempleOfTheFalseGod extends Card {

    public TempleOfTheFalseGod() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 2)),
                "{T}: Add {C}{C}. Activate only if you control five or more lands."
        ).withRequiredControlledPermanents(new PermanentIsLandPredicate(), 5, "lands"));
    }
}
