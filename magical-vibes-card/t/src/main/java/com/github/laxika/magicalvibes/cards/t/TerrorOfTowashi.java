package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MOM", collectorNumber = "331")
@CardRegistration(set = "MOM", collectorNumber = "378")
public class TerrorOfTowashi extends Card {

    public TerrorOfTowashi() {
        addEffect(EffectSlot.ON_ATTACK, MayPayManaEffect.reflexiveTarget(
                "{3}{B}",
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .grantSubtype(CardSubtype.PHYREXIAN)
                        .build(),
                "Pay {3}{B} to return target creature card from your graveyard to the battlefield?"));
    }
}
