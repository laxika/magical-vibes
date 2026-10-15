package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityEffect;

@CardRegistration(set = "YMID", collectorNumber = "20")
public class OglorDevotedAssistant extends Card {

    public OglorDevotedAssistant() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                LookAtTopCardsEffect.putOneIntoGraveyardRestOnTop(2));
        addEffect(EffectSlot.ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new PerpetuallyGrantTriggeredAbilityEffect(
                        CreateTokenEffect.blackZombie(1).withTapped(true)));
    }
}
