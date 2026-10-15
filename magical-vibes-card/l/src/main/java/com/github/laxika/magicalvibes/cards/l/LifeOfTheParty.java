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
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;
import java.util.Map;

@CardRegistration(set = "NCC", collectorNumber = "48")
@CardRegistration(set = "NCC", collectorNumber = "148")
public class LifeOfTheParty extends Card {

    public LifeOfTheParty() {
        PermanentCount creaturesYouControl = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(creaturesYouControl, new Fixed(0)));

        CreateTokenCopyOfTargetPermanentEffect goadedCopy =
                CreateTokenCopyOfTargetPermanentEffect.withAdditionalEffects(
                        false,
                        Map.of(EffectSlot.STATIC, List.of(new GoadCreaturesUntilNextTurnEffect(
                                new PermanentIsSourcePermanentPredicate()))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenCopyOfSourceForEachOtherPlayerEffect(goadedCopy));
    }
}
