package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.D20BasicLandPlacementEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForUpToTwoBasicLandsThenRollD20Effect;

@CardRegistration(set = "HBG", collectorNumber = "207")
public class DruidOfTheEmeraldGrove extends Card {

    public DruidOfTheEmeraldGrove() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryForUpToTwoBasicLandsThenRollD20Effect(
                        new D20BasicLandPlacementEffect(D20BasicLandPlacementEffect.Placement.TO_HAND),
                        new D20BasicLandPlacementEffect(
                                D20BasicLandPlacementEffect.Placement.ONE_TO_BATTLEFIELD_TAPPED),
                        new D20BasicLandPlacementEffect(
                                D20BasicLandPlacementEffect.Placement.ALL_TO_BATTLEFIELD_TAPPED)));
    }
}
