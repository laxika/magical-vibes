package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "MOC", collectorNumber = "243")
public class VedalkenHumiliator extends Card {

    public VedalkenHumiliator() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new Metalcraft(),
                SequenceEffect.of(
                        new LosesAllAbilitiesEffect(GrantScope.OPPONENT_CREATURES,
                                EffectDuration.UNTIL_END_OF_TURN),
                        new SetBasePowerToughnessEffect(1, 1, GrantScope.OPPONENT_CREATURES,
                                EffectDuration.UNTIL_END_OF_TURN)
                )));
    }
}
