package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "137")
@CardRegistration(set = "ONS", collectorNumber = "140")
@CardRegistration(set = "2XM", collectorNumber = "88")
public class DoomedNecromancer extends Card {

    public DoomedNecromancer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new SacrificeSelfCost(), ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.BATTLEFIELD).targetGraveyard(true).filter(new CardTypePredicate(CardType.CREATURE)).build()),
                "{B}, {T}, Sacrifice Doomed Necromancer: Return target creature card from your graveyard to the battlefield."
        ));
    }
}
