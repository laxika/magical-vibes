package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToNTargetPermanentsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "14")
public class DevastatingMastery extends Card {

    public DevastatingMastery() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{W}{W}"))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForAlternateCost(),
                ReturnUpToNTargetPermanentsToHandEffect.opponentChooses(2, nonland())));
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(nonland()));
    }

    private static PermanentNotPredicate nonland() {
        return new PermanentNotPredicate(new PermanentIsLandPredicate());
    }
}
