package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C18", collectorNumber = "11")
public class OctopusUmbra extends Card {

    public OctopusUmbra() {
        PermanentAllOfPredicate creatureWithPowerAtMostEight = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtMostPredicate(8)));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new SetBasePowerToughnessEffect(8, 8, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new TotemArmorEffect())
                .addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ATTACK,
                        new MayEffect(
                                new TapPermanentsEffect(TapUntapScope.TARGET, creatureWithPowerAtMostEight),
                                "Tap target creature with power 8 or less?"),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
