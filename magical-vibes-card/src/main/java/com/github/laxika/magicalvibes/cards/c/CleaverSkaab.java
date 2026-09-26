package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSacrificedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "834")
public class CleaverSkaab extends Card {

    public CleaverSkaab() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        SacrificePermanentCost.withPermanentSnapshot(
                                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), "another Zombie"),
                        new CreateTokenCopyOfSacrificedPermanentEffect(2)
                ),
                "{3}, {T}, Sacrifice another Zombie: Create two tokens that are copies of the sacrificed creature."
        ));
    }
}
