package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1096")
@CardRegistration(set = "SLD", collectorNumber = "1867")
public class SwordOfTruthAndJustice extends Card {

    public SwordOfTruthAndJustice() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(
                Set.of(CardColor.WHITE, CardColor.BLUE), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                SequenceEffect.of(
                        new PutCounterOnChosenOwnPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()),
                        new ProliferateEffect()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
