package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Target creature you control gets +2/+2 until end of turn.
 *
 * <p>Overload {5}{W} changes the target to each, so every creature its controller controls gets
 * +2/+2 and the overloaded spell chooses no targets.
 */
@CardRegistration(set = "MH1", collectorNumber = "32")
public class StirringAddress extends Card {

    public StirringAddress() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{5}{W}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new BoostTargetCreatureEffect(2, 2),
                new BoostAllOwnCreaturesEffect(2, 2)));
        target(TargetFilters.creatureYouControl());
    }
}
