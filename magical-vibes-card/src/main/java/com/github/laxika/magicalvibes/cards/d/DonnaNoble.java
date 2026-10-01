package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceOrPairedPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "WHO", collectorNumber = "82")
@CardRegistration(set = "WHO", collectorNumber = "382")
@CardRegistration(set = "WHO", collectorNumber = "687")
@CardRegistration(set = "WHO", collectorNumber = "973")
public class DonnaNoble extends Card {

    public DonnaNoble() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Donna Noble with another unpaired creature you control?"));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new SoulbondPairWithEnteringEffect());

        DealDamageToPlayersEffect reflectedDamage = new DealDamageToPlayersEffect(
                new EventValue(), DamageRecipient.TARGET_PLAYER)
                .withTriggeredTargetFilter(new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"));
        addEffect(EffectSlot.ON_ANY_PERMANENT_DEALT_DAMAGE,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceOrPairedPredicate(), reflectedDamage));
    }
}
