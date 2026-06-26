package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "20")
public class Seance extends Card {

    public Seance() {
        // At the beginning of each upkeep, you may exile target creature card from your graveyard.
        // If you do, create a token that's a copy of that card, except it's a Spirit in addition
        // to its other types. Exile it at the beginning of the next end step.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new MayEffect(
                        new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                true,
                                List.of(CardSubtype.SPIRIT),
                                false,
                                true
                        ),
                        "You may exile target creature card from your graveyard."
                ));
    }
}
