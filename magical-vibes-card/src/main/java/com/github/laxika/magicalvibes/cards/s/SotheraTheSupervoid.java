package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsNoPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureToExileWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "EOE", collectorNumber = "115")
public class SotheraTheSupervoid extends Card {

    public SotheraTheSupervoid() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new EachOpponentChoosesCreatureToExileWithSourceEffect());
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new AnyPlayerControlsNoPermanent(new PermanentIsCreaturePredicate()),
                        new SacrificeSelfThenEffect(
                                new ReturnCardExiledWithSourceToBattlefieldEffect(
                                        new CardTypePredicate(CardType.CREATURE), false, null,
                                        false, false, false, 2))));
    }
}
