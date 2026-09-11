package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "218")
public class IntrudingSoulrager extends Card {

    public IntrudingSoulrager() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{0}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.ROOM),
                                "Sacrifice a Room"),
                        new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT),
                        new DrawCardEffect(1)
                ),
                "{T}, Sacrifice a Room: This creature deals 2 damage to each opponent. Draw a card."
        ));
    }
}
