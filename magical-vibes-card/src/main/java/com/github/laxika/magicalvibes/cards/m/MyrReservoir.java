package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AwardMyrOnlyColorlessManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "183")
public class MyrReservoir extends Card {

    public MyrReservoir() {
        // {T}: Add {C}{C}. Spend this mana only to cast Myr spells or activate abilities of Myr.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardMyrOnlyColorlessManaEffect(2)),
                "{T}: Add {C}{C}. Spend this mana only to cast Myr spells or activate abilities of Myr."
        ));

        // {3}, {T}: Return target Myr card from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardSubtypePredicate(CardSubtype.MYR))
                        .targetGraveyard(true)
                        .build()),
                "{3}, {T}: Return target Myr card from your graveyard to your hand."
        ));
    }
}
