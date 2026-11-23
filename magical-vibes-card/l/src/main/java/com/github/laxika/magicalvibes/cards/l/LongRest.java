package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.amount.StartingLifeTotal;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;

@CardRegistration(set = "AFR", collectorNumber = "193")
public class LongRest extends Card {

    public LongRest() {
        setMultiTargetConstraint(MultiTargetConstraint.DIFFERENT_MANA_VALUES);
        addSpellEffect(new ReturnTargetCardsFromGraveyardToHandEffect(null, 0, true)
                .withReturnedCount());
        addSpellEffect(new ConditionalEffect(new EventValueAtLeast(8), new SetLifeTotalEffect(new StartingLifeTotal())));
        addSpellEffect(new ExileSpellEffect());
    }
}
