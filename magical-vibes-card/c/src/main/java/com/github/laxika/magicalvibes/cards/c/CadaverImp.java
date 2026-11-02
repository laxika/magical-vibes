package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "99")
@CardRegistration(set = "PC2", collectorNumber = "31")
@CardRegistration(set = "PCA", collectorNumber = "31")
@CardRegistration(set = "DDP", collectorNumber = "48")
@CardRegistration(set = "DDR", collectorNumber = "41")
@CardRegistration(set = "CMM", collectorNumber = "141")
public class CadaverImp extends Card {

    public CadaverImp() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build(), "Return the targeted creature card from your graveyard to your hand?"));
    }
}
