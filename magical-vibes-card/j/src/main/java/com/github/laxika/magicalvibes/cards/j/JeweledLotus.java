package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "396")
@CardRegistration(set = "CMM", collectorNumber = "611")
@CardRegistration(set = "CMM", collectorNumber = "702")
public class JeweledLotus extends Card {

    public JeweledLotus() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(),
                        new AwardAnyColorManaEffect(3, ManaSpendRestriction.COMMANDER_ONLY)),
                "{T}, Sacrifice this artifact: Add three mana of any one color. Spend this mana only to cast your commander."
        ));
    }
}
