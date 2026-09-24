package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2220")
public class PassionateArchaeologist extends Card {

    public PassionateArchaeologist() {
        PermanentPredicate commanderCreatureYouOwn = new PermanentAllOfPredicate(List.of(
                new PermanentIsCommanderPredicate(),
                new PermanentIsCreaturePredicate(),
                new PermanentOwnedBySourceControllerPredicate()));

        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        null,
                        List.of(new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER)),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent"),
                        new StackEntryCastFromZonePredicate(Zone.EXILE)),
                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                commanderCreatureYouOwn));
    }
}
