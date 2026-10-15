package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

public class BlackChocobo extends Card {

    public BlackChocobo() {
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE,
                new SearchLibraryEffect(new CardTypePredicate(CardType.LAND),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new BoostAllOwnCreaturesEffect(1, 0,
                        new PermanentHasSubtypePredicate(CardSubtype.BIRD)));
    }
}
