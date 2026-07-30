package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SearchZonesForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "162")
public class ArachnusSpinner extends Card {

    public ArachnusSpinner() {
        // Tap an untapped Spider you control: Search your graveyard and/or library for a card named
        // Arachnus Web and put it onto the battlefield attached to target creature. If you search
        // your library this way, shuffle.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.SPIDER)),
                        new SearchZonesForCardNamedToBattlefieldEffect("Arachnus Web", false, true)),
                "Tap an untapped Spider you control: Search your graveyard and/or library for a card named "
                        + "Arachnus Web and put it onto the battlefield attached to target creature. "
                        + "If you search your library this way, shuffle.",
                TargetFilters.creature()));
    }
}
