package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

public class DiviningDive extends Card {

    public DiviningDive() {
        addSpellEffect(seekCardsOfSecretlyChosenKind(1));
    }

    /** "Secretly choose land or nonland. Seek {@code count} cards of the chosen kind." */
    public static CardEffect seekCardsOfSecretlyChosenKind(int count) {
        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        return ChooseOneAtResolutionEffect.secretly(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Land", new SeekLibraryEffect(count, land)),
                new ChooseOneEffect.ChooseOneOption("Nonland",
                        new SeekLibraryEffect(count, new CardNotPredicate(land))))));
    }
}
