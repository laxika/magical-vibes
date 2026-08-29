package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseNonlandCardNameRevealTopCardsToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentEffectsCantCauseDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentEffectsCantCauseSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "220")
public class TamiyoCollectorOfTales extends Card {

    public TamiyoCollectorOfTales() {
        addEffect(EffectSlot.STATIC, new OpponentEffectsCantCauseDiscardEffect());
        addEffect(EffectSlot.STATIC, new OpponentEffectsCantCauseSacrificeEffect());

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new ChooseNonlandCardNameRevealTopCardsToHandRestToGraveyardEffect(4)),
                "+1: Choose a nonland card name, then reveal the top four cards of your library. Put all cards with the chosen name from among them into your hand and the rest into your graveyard."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .targetGraveyard(true)
                        .build()),
                "−3: Return target card from your graveyard to your hand."
        ));
    }
}
