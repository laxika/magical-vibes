package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerAttackedControllerDuringLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "MB2", collectorNumber = "138")
public class OKagachiVengefulKami extends Card {

    public OKagachiVengefulKami() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ConditionalEffect(
                        new TargetPlayerAttackedControllerDuringLastTurn(),
                        new ExilePermanentDamagedPlayerControlsEffect(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()))));
    }
}
