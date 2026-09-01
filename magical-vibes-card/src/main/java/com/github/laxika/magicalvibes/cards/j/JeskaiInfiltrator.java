package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndTopCardThenManifestEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "FRF", collectorNumber = "36")
public class JeskaiInfiltrator extends Card {

    public JeskaiInfiltrator() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NoOtherPermanent(new PermanentIsCreaturePredicate()),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileSelfAndTopCardThenManifestEffect());
    }
}
