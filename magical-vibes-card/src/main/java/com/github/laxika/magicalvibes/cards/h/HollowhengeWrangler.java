package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "51")
public class HollowhengeWrangler extends Card {

    public HollowhengeWrangler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SeekCardsToHandEffect(new Fixed(1), new CardTypePredicate(CardType.LAND)));

        addActivatedAbility(beastAbility());
        addGraveyardActivatedAbility(beastAbility());
    }

    private static ActivatedAbility beastAbility() {
        return new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.LAND), "land"),
                        new ConjureCardNamedIntoHandEffect("Hollowhenge Beast", false)
                ),
                "Discard a land card: Conjure a card named Hollowhenge Beast into your hand. "
                        + "You may also activate this ability while Hollowhenge Wrangler is in your graveyard."
        );
    }
}
