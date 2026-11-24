package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RollTwoDiceChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFC", collectorNumber = "43")
public class WildEndeavor extends Card {

    public WildEndeavor() {
        addSpellEffect(new RollTwoDiceChooseOneEffect(4));
        addSpellEffect(new CreateTokenEffect(new XValue(), "Beast", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.BEAST), Set.of(), Set.of()));
        addSpellEffect(new SearchLibraryEffect(new EventValue(), CardPredicateUtils.basicLand(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
