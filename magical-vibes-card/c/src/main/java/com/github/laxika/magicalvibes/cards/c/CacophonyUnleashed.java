package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "730")
@CardRegistration(set = "CMM", collectorNumber = "761")
public class CacophonyUnleashed extends Card {

    public CacophonyUnleashed() {
        // When this enchantment enters, if you cast it, destroy all nonenchantment creatures.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new WasCast()),
                new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsEnchantmentPredicate())))));

        // Whenever this enchantment or another enchantment you control enters, this enchantment
        // becomes a legendary 6/6 Nightmare God creature with menace and deathtouch until end of turn.
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.enters(Triggers.ENCHANTMENT,
                                ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(animationEffect()))));
    }

    private SequenceEffect animationEffect() {
        return SequenceEffect.of(
                new AnimatePermanentsEffect(6, 6,
                        List.of(CardSubtype.NIGHTMARE, CardSubtype.GOD),
                        Set.of(Keyword.MENACE, Keyword.DEATHTOUCH)),
                new GrantSupertypeUntilEndOfTurnEffect(CardSupertype.LEGENDARY, GrantScope.SELF));
    }
}
