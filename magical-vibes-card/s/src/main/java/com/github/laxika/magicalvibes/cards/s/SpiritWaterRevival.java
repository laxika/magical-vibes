package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.WaterbendCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeDuration;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;

@CardRegistration(set = "TLA", collectorNumber = "73")
public class SpiritWaterRevival extends Card {

    public SpiritWaterRevival() {
        addCastingCost(WaterbendCost.optional(6));
        addSpellEffect(new ConditionalReplacementEffect(
                new WaterbendCostPaid(),
                new DrawCardEffect(2),
                SequenceEffect.of(
                        new ShuffleGraveyardIntoLibraryEffect(false),
                        new DrawCardEffect(7),
                        new GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration.REST_OF_GAME)
                )
        ));
        addSpellEffect(new ExileSpellEffect());
    }
}
