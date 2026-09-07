package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowPlayAndCastCardsExiledWithCroakCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringPermanentCardFromLibraryWithCroakCounterEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "VOW", collectorNumber = "238")
public class GrolnokTheOmnivore extends Card {

    public GrolnokTheOmnivore() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.FROG),
                        new MillEffect(3, MillRecipient.CONTROLLER)));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
                new ExileTriggeringPermanentCardFromLibraryWithCroakCounterEffect());
        addEffect(EffectSlot.STATIC, new AllowPlayAndCastCardsExiledWithCroakCountersEffect());
    }
}
