package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "30")
@CardRegistration(set = "DSC", collectorNumber = "58")
public class CuratorBeastie extends Card {

    public CuratorBeastie() {
        addEffect(EffectSlot.STATIC, new ControlledPermanentsEnterWithAdditionalCountersEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsColorlessPredicate())),
                2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ManifestDreadEffect.forController());
        addEffect(EffectSlot.ON_ATTACK, ManifestDreadEffect.forController());
    }
}
