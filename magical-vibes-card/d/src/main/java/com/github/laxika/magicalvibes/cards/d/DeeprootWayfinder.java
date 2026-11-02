package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "184")
@CardRegistration(set = "MOM", collectorNumber = "369")
public class DeeprootWayfinder extends Card {

    public DeeprootWayfinder() {
        addTrigger(Triggers.dealsCombatDamageToPlayerOrBattle(),
                SurveilThenEffect.direct(
                        1,
                        new MayEffect(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(new CardTypePredicate(CardType.LAND))
                                        .enterTapped(true)
                                        .build(),
                                "Return a land card from your graveyard to the battlefield tapped?")));
    }
}
