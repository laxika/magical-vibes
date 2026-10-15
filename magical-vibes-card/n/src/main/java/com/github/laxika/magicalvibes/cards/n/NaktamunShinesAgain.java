package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PerpetualBoostOwnCreatureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;

import java.util.List;

@CardRegistration(set = "YDFT", collectorNumber = "2")
public class NaktamunShinesAgain extends Card {

    public NaktamunShinesAgain() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new PerpetualBoostOwnCreatureCardsEffect(2, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new SeekLibraryEffect(new CardTypePredicate(CardType.CREATURE), 2));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentMaxManaValuePredicate(2)))));
    }
}
