package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromEverythingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

@CardRegistration(set = "40K", collectorNumber = "19")
public class VexilusPraetor extends Card {

    public VexilusPraetor() {
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new ProtectionFromEverythingEffect(),
                GrantScope.OWN_PERMANENTS,
                new PermanentIsCommanderPredicate()));
    }
}
