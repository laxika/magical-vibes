package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "TMT", collectorNumber = "126")
@CardRegistration(set = "TMT", collectorNumber = "240")
public class NewGenerationsTechnique extends Card {

    public NewGenerationsTechnique() {
        addSneak("{2}{G}");
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new Fixed(2), CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
