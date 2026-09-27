package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RingTemptsYouEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

@CardRegistration(set = "LTC", collectorNumber = "4")
@CardRegistration(set = "LTC", collectorNumber = "84")
@CardRegistration(set = "LTC", collectorNumber = "92")
public class SauronLordOfTheRings extends Card {

    public SauronLordOfTheRings() {
        addEffect(EffectSlot.ON_SELF_CAST, new AmassGoblinsEffect(5, CardSubtype.ORC));
        addEffect(EffectSlot.ON_SELF_CAST, new MillEffect(5, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_SELF_CAST, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .mandatory(true)
                .build());

        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCommanderPredicate(), new RingTemptsYouEffect()));
    }
}
