package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "5")
public class HandOfEmrakul extends Card {

    public HandOfEmrakul() {
        addCastingOption(new AlternateHandCast(List.of(
                new SacrificePermanentsCost(4, new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.ELDRAZI),
                        new PermanentHasSubtypePredicate(CardSubtype.SPAWN)))))));

        addTrigger(Triggers.attacks(), new SacrificePermanentsEffect(
                1, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));
    }
}
