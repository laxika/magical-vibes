package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "75")
public class AbhorrentOverlord extends Card {

    public AbhorrentOverlord() {
        // When this creature enters, create Harpies equal to your black devotion.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.BLACK),
                "Harpy", 1, 1, CardColor.BLACK, List.of(CardSubtype.HARPY),
                Set.of(Keyword.FLYING), Set.of()));
        // At the beginning of your upkeep, sacrifice a creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.CONTROLLER));
    }
}
