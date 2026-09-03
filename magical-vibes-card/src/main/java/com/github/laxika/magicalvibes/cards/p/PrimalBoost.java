package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "277")
public class PrimalBoost extends Card {

    public PrimalBoost() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 4));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new BoostTargetCreatureEffect(1, 1), new DrawCardEffect(1)),
                "Cycling {2}{G} ({2}{G}, Discard this card: Draw a card.)",
                TargetFilters.creature(),
                null,
                null,
                null,
                List.of(),
                0,
                1));
    }
}
