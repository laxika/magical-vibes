package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "153")
public class GrazingKelpie extends Card {

    public GrazingKelpie() {
        // {G/U}, Sacrifice this creature: Put target card from a graveyard on the bottom of its owner's library.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G/U}",
                List.of(new SacrificeSelfCost(), ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .targetGraveyard(true)
                        .build()),
                "{G/U}, Sacrifice Grazing Kelpie: Put target card from a graveyard on the bottom of its owner's library."
        ));

        // Persist is loaded from Scryfall as Keyword.PERSIST; the engine handles the return in
        // PermanentRemovalService.collectPersistTrigger + PersistReturnEffect.
    }
}
