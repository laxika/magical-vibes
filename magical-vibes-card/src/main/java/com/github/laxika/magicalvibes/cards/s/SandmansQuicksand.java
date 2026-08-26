package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "63")
public class SandmansQuicksand extends Card {

    public SandmansQuicksand() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForAlternateCost(),
                new BoostAllCreaturesEffect(-2, -2,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()),
                new BoostAllCreaturesEffect(-2, -2)));
        addCastingOption(new GraveyardCast(null, "{3}{B}", List.of(), new CardDiscardedThisTurn()));
    }
}
