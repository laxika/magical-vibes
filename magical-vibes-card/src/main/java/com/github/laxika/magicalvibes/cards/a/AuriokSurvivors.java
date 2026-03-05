package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "NPH", collectorNumber = "3")
public class AuriokSurvivors extends Card {

    public AuriokSurvivors() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ReturnCardFromGraveyardEffect(
                        GraveyardChoiceDestination.BATTLEFIELD,
                        new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false, false, false, null, false, true
                ),
                "Return an Equipment card from your graveyard to the battlefield and attach it to this creature?"
        ));
    }
}
