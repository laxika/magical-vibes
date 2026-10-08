package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCardTypeFromAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "8")
public class CloudsteelKirin extends Card {

    public CloudsteelKirin() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantEffectEffect(new CantLoseGameEffect(), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new RemoveCardTypeFromAttachedPermanentEffect(CardType.CREATURE, GrantScope.SELF,
                        EffectDuration.WHILE_ATTACHED));

        var reconfigureFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        var reconfigureTarget = TargetPredicates.permanents(reconfigureFilter);
        addActivatedAbility(new ActivatedAbility(false, "{5}",
                List.of(EquipEffect.reconfigure(reconfigureTarget)),
                "Reconfigure {5}", new PermanentPredicateTargetFilter(reconfigureFilter, "Target must be another creature you control."), null, null,
                ActivationTimingRestriction.SORCERY_SPEED));
        addActivatedAbility(new ActivatedAbility(false, "{5}",
                List.of(UnattachEquipmentEffect.source()),
                "Reconfigure {5} (unattach)", ActivationTimingRestriction.SORCERY_SPEED));
    }
}
