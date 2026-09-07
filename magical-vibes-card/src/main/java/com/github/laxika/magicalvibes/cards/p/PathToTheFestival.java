package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.BasicLandTypesAmongControlledLandsAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "MID", collectorNumber = "191")
public class PathToTheFestival extends Card {

    public PathToTheFestival() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new BasicLandTypesAmongControlledLandsAtLeast(3), new ScryEffect(1)));
    }
}
