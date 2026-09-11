package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardWithConditionalEffectsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "26")
public class LionSash extends Card {

    public LionSash() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new ExileTargetCardFromGraveyardWithConditionalEffectsEffect(
                        new CardIsPermanentPredicate(),
                        new PutCountersOnSourceEffect(1, 1, 1),
                        new PutCountersOnSourceEffect(0, 0, 0))),
                "{W}: Exile target card from a graveyard. If it was a permanent card, put a +1/+1 counter on Lion Sash."));

        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                GrantScope.EQUIPPED_CREATURE));

        addEffect(EffectSlot.STATIC, new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.SELF,
                EffectDuration.WHILE_ATTACHED));

        var reconfigureTarget = TargetPredicates.permanents(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate())));
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(EquipEffect.reconfigure(reconfigureTarget)),
                "Reconfigure {2}", TargetFilters.creatureYouControl(), null, null,
                ActivationTimingRestriction.SORCERY_SPEED));
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(UnattachEquipmentEffect.source()),
                "Reconfigure {2} (unattach)", ActivationTimingRestriction.SORCERY_SPEED));
    }
}
