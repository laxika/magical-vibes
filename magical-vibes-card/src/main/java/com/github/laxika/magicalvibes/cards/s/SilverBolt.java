package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureThenDestroyIfDamagedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "258")
public class SilverBolt extends Card {

    public SilverBolt() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new SacrificeSelfCost(),
                        new DealDamageToTargetCreatureThenDestroyIfDamagedEffect(
                                3, new PermanentHasSubtypePredicate(CardSubtype.WEREWOLF))
                ),
                "{3}, {T}, Sacrifice this artifact: It deals 3 damage to target creature. If a Werewolf is dealt damage this way, destroy it.",
                TargetFilters.creature()
        ));
    }
}
