package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;

@CardRegistration(set = "ORI", collectorNumber = "183")
public class JoragaInvocation extends Card {

    public JoragaInvocation() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(3, 3));
        addEffect(EffectSlot.SPELL,
                new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BE_BLOCKED, GrantScope.ALL_OWN_CREATURES));
    }
}
