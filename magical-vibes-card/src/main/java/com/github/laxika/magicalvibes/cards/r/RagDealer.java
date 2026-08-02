package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "138")
public class RagDealer extends Card {

    public RagDealer() {
        // {2}{B}, {T}: Exile up to three target cards from a single graveyard.
        // Targets are chosen on activation and ride in on the ability's graveyard target ids;
        // TargetLegalityService enforces the "single graveyard" restriction.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(new ExileGraveyardCardsEffect(3, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD)),
                "{2}{B}, {T}: Exile up to three target cards from a single graveyard."
        ));
    }
}
