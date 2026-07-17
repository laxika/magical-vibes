package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "64")
public class ArchdemonOfUnx extends Card {

    public ArchdemonOfUnx() {
        // At the beginning of your upkeep, sacrifice a non-Zombie creature, then create a
        // 2/2 black Zombie creature token. The two instructions are unlinked: the token is
        // created regardless of whether a creature was sacrificed.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificePermanentsEffect(
                1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))
                )),
                SacrificeRecipient.CONTROLLER));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenEffect(
                "Zombie", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE),
                Set.of(), Set.of()));
    }
}
