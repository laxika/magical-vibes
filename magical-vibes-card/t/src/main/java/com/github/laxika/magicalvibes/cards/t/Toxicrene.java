package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllNonManaAbilitiesEffect;

@CardRegistration(set = "40K", collectorNumber = "101")
public class Toxicrene extends Card {

    public Toxicrene() {
        addEffect(EffectSlot.STATIC, new LosesAllNonManaAbilitiesEffect(GrantScope.ALL_LANDS));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(), GrantScope.ALL_LANDS));
    }
}
