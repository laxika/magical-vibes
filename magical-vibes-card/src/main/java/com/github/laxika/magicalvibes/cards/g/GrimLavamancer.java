package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "145")
@CardRegistration(set = "TOR", collectorNumber = "100")
public class GrimLavamancer extends Card {

    public GrimLavamancer() {
        // "{R}, {T}, Exile two cards from your graveyard: This creature deals 2 damage to any target."
        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(new ExileNCardsFromGraveyardCost(2, null), new DealDamageToAnyTargetEffect(2)),
                "{R}, {T}, Exile two cards from your graveyard: Grim Lavamancer deals 2 damage to any target."));
    }
}
