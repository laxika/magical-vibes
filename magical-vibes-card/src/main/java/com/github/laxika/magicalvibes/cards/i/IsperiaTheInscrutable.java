package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameRevealHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "114")
public class IsperiaTheInscrutable extends Card {

    public IsperiaTheInscrutable() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ChooseCardNameRevealHandThenEffect(
                new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardKeywordPredicate(Keyword.FLYING))))));
    }
}
