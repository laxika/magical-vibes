package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryMatchesPredicate;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "22")
public class OuraTheImitator extends Card {

    public OuraTheImitator() {
        CardSubtypePredicate faerie = new CardSubtypePredicate(CardSubtype.FAERIE);
        CardAnyOfPredicate faerieOrInstant = new CardAnyOfPredicate(List.of(faerie, new CardTypePredicate(CardType.INSTANT)));
        addStaticEffect(new ConditionalEffect(
                new TopCardOfLibraryMatchesPredicate(faerieOrInstant),
                new LookAtTopCardOfOwnLibraryEffect()));
        addStaticEffect(new AllowCastFromTopOfLibraryEffect(faerieOrInstant));
        addStaticEffect(new GrantFlashToCardTypeEffect(faerie));
    }
}
