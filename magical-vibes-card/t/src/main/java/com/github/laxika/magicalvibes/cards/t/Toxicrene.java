package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;

@CardRegistration(set = "40K", collectorNumber = "101")
public class Toxicrene extends Card {

    public Toxicrene() {
        addEffect(EffectSlot.STATIC, new LosesAllAbilitiesEffect(GrantScope.ALL_LANDS_INCLUDING_SELF));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(), GrantScope.ALL_LANDS_INCLUDING_SELF));
    }
}
