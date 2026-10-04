package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "315")
@CardRegistration(set = "WOE", collectorNumber = "372")
public class MalevolentWitchkite extends Card {

    public MalevolentWitchkite() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsEnchantmentPredicate(),
                                new PermanentIsTokenPredicate()))));
    }
}
