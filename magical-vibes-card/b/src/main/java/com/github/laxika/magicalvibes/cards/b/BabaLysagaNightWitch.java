package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "232")
public class BabaLysagaNightWitch extends Card {

    public BabaLysagaNightWitch() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeAnyNumberOfPermanentsCost(
                                new PermanentTruePredicate(), false, false, 3, true),
                        new ConditionalEffect(
                                new SpellXAtLeast(3),
                                new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT)),
                        new ConditionalEffect(new SpellXAtLeast(3), new GainLifeEffect(3)),
                        new ConditionalEffect(new SpellXAtLeast(3), new DrawCardEffect(3))),
                "{T}, Sacrifice up to three permanents: If there were three or more card types among "
                        + "the sacrificed permanents, each opponent loses 3 life, you gain 3 life, and you "
                        + "draw three cards."
        ));
    }
}
