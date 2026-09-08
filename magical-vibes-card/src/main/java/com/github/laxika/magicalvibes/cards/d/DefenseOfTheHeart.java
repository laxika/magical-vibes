package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanentCount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ULG", collectorNumber = "100")
public class DefenseOfTheHeart extends Card {

    public DefenseOfTheHeart() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new OpponentControlsPermanentCount(3, new PermanentIsCreaturePredicate()),
                SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        new SearchLibraryEffect(new Fixed(2), new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD))));
    }
}
