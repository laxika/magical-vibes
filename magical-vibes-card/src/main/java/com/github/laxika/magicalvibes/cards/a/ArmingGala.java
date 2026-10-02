package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOtherControlledCreaturesAndCardsInHandAndLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YSNC", collectorNumber = "16")
public class ArmingGala extends Card {

    public ArmingGala() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                new PerpetuallyBoostOtherControlledCreaturesAndCardsInHandAndLibraryEffect(
                        new CardTypePredicate(CardType.CREATURE), 1, 1),
                new PerpetuallyBoostCreatureCardsInGraveyardEffect(1, 1)));
    }
}
