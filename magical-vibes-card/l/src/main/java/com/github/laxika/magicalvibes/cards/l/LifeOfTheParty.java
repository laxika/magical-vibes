package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreatedPermanentsForRestOfGameEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Map;

@CardRegistration(set = "NCC", collectorNumber = "48")
@CardRegistration(set = "NCC", collectorNumber = "148")
public class LifeOfTheParty extends Card {

    public LifeOfTheParty() {
        PermanentCount creaturesYouControl = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(creaturesYouControl, new Fixed(0)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new CreateTokenCopyOfSourceForEachOtherPlayerEffect(
                        CreateTokenCopyOfTargetPermanentEffect.withAdditionalEffects(false, Map.of())),
                new GoadCreatedPermanentsForRestOfGameEffect()));
    }
}
