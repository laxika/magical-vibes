package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "160")
@CardRegistration(set = "WOT", collectorNumber = "33")
@CardRegistration(set = "DMR", collectorNumber = "96")
@CardRegistration(set = "FDC", collectorNumber = "122")
public class OversoldCemetery extends Card {

    public OversoldCemetery() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new GraveyardCardThreshold(4, new CardTypePredicate(CardType.CREATURE))),
                new MayEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build(), "Return the targeted creature card to your hand?"));
    }
}
