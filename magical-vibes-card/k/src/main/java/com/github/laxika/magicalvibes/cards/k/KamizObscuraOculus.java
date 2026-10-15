package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherAttackingCreatureWithLesserPowerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NCC", collectorNumber = "3")
@CardRegistration(set = "NCC", collectorNumber = "103")
@CardRegistration(set = "NCC", collectorNumber = "188")
public class KamizObscuraOculus extends Card {

    public KamizObscuraOculus() {
        target(TargetFilters.attackingCreature()).addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                SequenceEffect.of(
                        new MakeCreatureUnblockableEffect(),
                        new DrawDiscardAndConniveEffect(true),
                        new ChooseAnotherAttackingCreatureWithLesserPowerEffect()));
    }
}
