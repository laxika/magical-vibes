package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;

@CardRegistration(set = "LCI", collectorNumber = "187")
public class GlowcapLantern extends Card {

    public GlowcapLantern() {
        addStaticEffect(new GrantEffectEffect(new LookAtTopCardOfOwnLibraryEffect(), GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ATTACK,
                new ExploreEffect(),
                GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
