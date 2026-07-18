package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Raise Dead — the prepare spell (inset) of Cheerful Osteomancer // Raise Dead (SOS 76).
 * <p>
 * Sorcery: Return target creature card from your graveyard to your hand.
 * <p>
 * Not independently registered: its oracle data is registered for the class name "RaiseDead" when
 * Cheerful Osteomancer (SOS 76) loads (see {@code CheerfulOsteomancerRaiseDead#getBackFaceClassName}). A copy of
 * this spell is created in exile while Cheerful Osteomancer is prepared and may be cast from there.
 */
@CardRegistration(set = "6ED", collectorNumber = "152")
@CardRegistration(set = "7ED", collectorNumber = "157")
@CardRegistration(set = "8ED", collectorNumber = "157")
@CardRegistration(set = "9ED", collectorNumber = "156")
@CardRegistration(set = "POR", collectorNumber = "107")
@CardRegistration(set = "P02", collectorNumber = "86")
@CardRegistration(set = "5ED", collectorNumber = "191")
@CardRegistration(set = "4ED", collectorNumber = "156")
public class RaiseDead extends Card {

    public RaiseDead() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build());
    }
}
