package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "1")
public class AdarkarUnicorn extends Card {

    public AdarkarUnicorn() {
        // {T}: Add {U} or {C}{U}. Spend this mana only to pay cumulative upkeep costs.
        // Two tap abilities model the printed modes (same pattern as Elfhame Druid).
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(ManaColor.BLUE, 1, new ManaRestriction.CumulativeUpkeepCosts())),
                "{T}: Add {U}. Spend this mana only to pay cumulative upkeep costs."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardRestrictedManaEffect(ManaColor.COLORLESS, 1, new ManaRestriction.CumulativeUpkeepCosts()),
                        new AwardRestrictedManaEffect(ManaColor.BLUE, 1, new ManaRestriction.CumulativeUpkeepCosts())
                ),
                "{T}: Add {C}{U}. Spend this mana only to pay cumulative upkeep costs."
        ));
    }
}
