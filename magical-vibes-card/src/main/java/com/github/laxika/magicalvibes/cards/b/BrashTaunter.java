package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "133")
@CardRegistration(set = "M21", collectorNumber = "363")
@CardRegistration(set = "SLD", collectorNumber = "1614")
@CardRegistration(set = "DSC", collectorNumber = "161")
@CardRegistration(set = "MKC", collectorNumber = "148")
@CardRegistration(set = "SCD", collectorNumber = "125")
public class BrashTaunter extends Card {

    public BrashTaunter() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER)
                        .withTriggeredTargetFilter(new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent")));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new SourceFightsTargetCreatureEffect()),
                "{2}{R}, {T}: This creature fights another target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        "Target must be another creature")));
    }
}
