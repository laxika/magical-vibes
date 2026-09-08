package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "98")
public class KarumonixTheRatKing extends Card {

    public KarumonixTheRatKing() {
        PermanentAllOfPredicate rat = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.RAT)
        ));

        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.TOXIC, GrantScope.OWN_CREATURES, rat));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LookAtTopCardsEffect(
                        new Fixed(5),
                        new Fixed(5),
                        new CardSubtypePredicate(CardSubtype.RAT),
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                        false,
                        LibrarySearchDestination.HAND,
                        true));
    }
}
