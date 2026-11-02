package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RingTemptsYouEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "4")
@CardRegistration(set = "LTC", collectorNumber = "84")
@CardRegistration(set = "LTC", collectorNumber = "92")
@CardRegistration(set = "LTC", collectorNumber = "478")
public class SauronLordOfTheRings extends Card {

    public SauronLordOfTheRings() {
        addTrigger(Triggers.youCastThisSpell(), new AmassGoblinsEffect(5, CardSubtype.ORC));
        addTrigger(Triggers.youCastThisSpell(), new MillEffect(5, MillRecipient.CONTROLLER));
        addTrigger(Triggers.youCastThisSpell(), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .mandatory(true)
                .build());

        addTrigger(Triggers.dies(new PermanentIsCommanderPredicate(), ObjectRelation.OPPONENT_CONTROLS),
                new RingTemptsYouEffect());
    }
}
