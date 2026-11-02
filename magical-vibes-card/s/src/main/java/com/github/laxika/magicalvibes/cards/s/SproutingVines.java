package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "128")
@CardRegistration(set = "C13", collectorNumber = "173")
public class SproutingVines extends Card {

    public SproutingVines() {
        addSpellEffect(new SearchLibraryEffect(
                CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND));
        addTrigger(Triggers.youCastThisSpell(), new StormEffect());
    }
}
