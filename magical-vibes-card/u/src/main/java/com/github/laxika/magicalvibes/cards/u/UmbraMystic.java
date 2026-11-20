package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToPermanentControlledBySourceControllerPredicate;

@CardRegistration(set = "ROE", collectorNumber = "52")
@CardRegistration(set = "WOC", collectorNumber = "82")
public class UmbraMystic extends Card {

    public UmbraMystic() {
        addStaticEffect(new GrantEffectEffect(
                new TotemArmorEffect(), Subjects.otherPermanents(),
                new PermanentIsAuraAttachedToPermanentControlledBySourceControllerPredicate()));
    }
}
