package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

/**
 * Destroy target creature. A creature destroyed this way can't be regenerated.
 *
 * <p>Overload replaces the targeted destruction with destruction of every creature and therefore
 * does not choose a target.
 */

@CardRegistration(set = "SLD", collectorNumber = "1870")
@CardRegistration(set = "AA2", collectorNumber = "8")
@CardRegistration(set = "MH2", collectorNumber = "80")
public class Damn extends Card {

    public Damn() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{W}{W}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new DestroyTargetPermanentEffect(true),
                new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate(), true)));
        target(TargetFilters.creature());
    }
}
