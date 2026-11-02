package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C14", collectorNumber = "50")
@CardRegistration(set = "MH2", collectorNumber = "287")
@CardRegistration(set = "OTC", collectorNumber = "210")
@CardRegistration(set = "MB2", collectorNumber = "249")
@CardRegistration(set = "EOC", collectorNumber = "111")
@CardRegistration(set = "CMA", collectorNumber = "157")
public class TitaniaProtectorOfArgoth extends Card {

    public TitaniaProtectorOfArgoth() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.LAND))
                .targetGraveyard(true)
                .build());

        addTrigger(Triggers.dies(new PermanentIsLandPredicate(), ObjectRelation.YOU_CONTROL),
                new CreateTokenEffect("Elemental", 5, 3,
                        CardColor.GREEN, List.of(CardSubtype.ELEMENTAL),
                        Set.of(), Set.of()));
    }
}
