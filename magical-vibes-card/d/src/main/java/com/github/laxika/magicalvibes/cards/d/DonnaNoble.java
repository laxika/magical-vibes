package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPairedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceOrPairedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "82")
@CardRegistration(set = "WHO", collectorNumber = "382")
@CardRegistration(set = "WHO", collectorNumber = "687")
@CardRegistration(set = "WHO", collectorNumber = "973")
public class DonnaNoble extends Card {

    public DonnaNoble() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Donna Noble with another unpaired creature you control?"));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new AllConditions(List.of(
                        new NotCondition(new SourceIsPaired()),
                        new TriggeringPermanentMatches(new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentIsPairedPredicate()))))))),
                new MayEffect(new SoulbondPairWithEnteringEffect(),
                        "Pair Donna Noble with that creature?"));

        target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addTrigger(Triggers.permanentYouControlDealtDamage()
                                .withFilter(new PermanentIsSourceOrPairedPredicate()),
                        new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER));
    }
}
