package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndTopCardThenManifestEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "36")
@CardRegistration(set = "C18", collectorNumber = "93")
public class JeskaiInfiltrator extends Card {

    public JeskaiInfiltrator() {
        addStaticEffect(new ConditionalEffect(
                new NoOtherPermanent(new PermanentIsCreaturePredicate()),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileSelfAndTopCardThenManifestEffect());
    }
}
