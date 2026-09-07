package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import java.util.List;

public class AvatarYangchen extends Card {

    public AvatarYangchen() {
        PermanentPredicate anotherNonland = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        PermanentPredicateTargetFilter airbendTarget = new PermanentPredicateTargetFilter(
                anotherNonland, "Target must be another nonland permanent");
        target(airbendTarget, 0, 1)
                .addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                        SpellCastTriggerEffect.nth(2, null,
                                List.of(new AirbendTargetPermanentEffect()), airbendTarget));
    }
}
