package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "132")
public class FurnacePunisher extends Card {

    public FurnacePunisher() {
        var basicLandControlledByActivePlayer = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.BASIC),
                new PermanentControlledByActivePlayerPredicate()));
        var activePlayerControlsTwoBasicLands = new AnyPlayerControlsPermanentCount(
                2, basicLandControlledByActivePlayer);

        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, ConditionalEffect.unless(
                new NotCondition(activePlayerControlsTwoBasicLands),
                new DealDamageToPlayersEffect(2, DamageRecipient.ACTIVE_PLAYER)));
    }
}
