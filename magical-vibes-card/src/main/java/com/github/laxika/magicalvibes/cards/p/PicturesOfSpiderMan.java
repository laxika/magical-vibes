package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "109")
public class PicturesOfSpiderMan extends Card {

    public PicturesOfSpiderMan() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LookAtTopCardsEffect(
                        new Fixed(5),
                        new Fixed(2),
                        new CardTypePredicate(CardType.CREATURE),
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                        false,
                        LibrarySearchDestination.HAND,
                        true));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), CreateTokenEffect.ofTreasureToken(1)),
                "{1}, {T}, Sacrifice this artifact: Create a Treasure token."));
    }
}
