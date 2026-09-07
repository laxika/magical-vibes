package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "327")
public class UnholyGrotto extends Card {

    public UnholyGrotto() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {B}, {T}: Put target Zombie card from your graveyard on top of your library.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                        .filter(new CardSubtypePredicate(CardSubtype.ZOMBIE))
                        .targetGraveyard(true)
                        .build()),
                "{B}, {T}: Put target Zombie card from your graveyard on top of your library."
        ));
    }
}
