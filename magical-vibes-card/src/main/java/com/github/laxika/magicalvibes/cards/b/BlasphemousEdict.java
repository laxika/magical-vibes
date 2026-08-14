package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "57")
public class BlasphemousEdict extends Card {

    public BlasphemousEdict() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{B}")),
                new AnyPlayerControlsPermanentCount(13, new PermanentIsCreaturePredicate()),
                false));
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                13,
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                SacrificeRecipient.EACH_PLAYER));
    }
}
