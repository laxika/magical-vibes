package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "181")
public class RamunapRuins extends Card {

    public RamunapRuins() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {T}, Pay 1 life: Add {R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new AwardManaEffect(ManaColor.RED)),
                "{T}, Pay 1 life: Add {R}."
        ));

        // {2}{R}{R}, {T}, Sacrifice a Desert: This land deals 2 damage to each opponent.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}{R}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.DESERT),
                                "Sacrifice a Desert",
                                false),
                        new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)),
                "{2}{R}{R}, {T}, Sacrifice a Desert: This land deals 2 damage to each opponent."
        ));
    }
}
