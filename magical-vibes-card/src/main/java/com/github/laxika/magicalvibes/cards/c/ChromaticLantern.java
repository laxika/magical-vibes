package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "RTR", collectorNumber = "226")
public class ChromaticLantern extends Card {

    public ChromaticLantern() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(), GrantScope.OWN_LANDS));
    }
}
