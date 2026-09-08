package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsCantBeCastEffect;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "138")
public class NullhideFerox extends Card {

    public NullhideFerox() {
        addEffect(EffectSlot.STATIC, new NoncreatureSpellsCantBeCastEffect(0, false, false));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new LosesAllAbilitiesEffect(GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN)),
                "{2}: This creature loses all abilities until end of turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT, new EnterBattlefieldOnDiscardEffect());
    }
}
