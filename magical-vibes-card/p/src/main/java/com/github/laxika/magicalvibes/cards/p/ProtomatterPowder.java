package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ALA", collectorNumber = "53")
public class ProtomatterPowder extends Card {

    public ProtomatterPowder() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{W}",
                List.of(new SacrificeSelfCost(), ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.BATTLEFIELD).filter(new CardTypePredicate(CardType.ARTIFACT)).build()),
                "{4}{W}, {T}, Sacrifice Protomatter Powder: Return target artifact card from your graveyard to the battlefield."
        ));
    }
}
