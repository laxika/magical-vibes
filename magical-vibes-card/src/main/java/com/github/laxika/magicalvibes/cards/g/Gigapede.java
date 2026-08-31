package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "ONS", collectorNumber = "264")
public class Gigapede extends Card {

    public Gigapede() {
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new SourceCardInGraveyard(),
                        new MayEffect(
                                new DiscardCardThenEffect(
                                        null,
                                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                                        "a card"),
                                "Discard a card to return Gigapede from your graveyard to your hand?")));
    }
}
