package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "229")
public class GraazUnstoppableJuggernaut extends Card {

    public GraazUnstoppableJuggernaut() {
        addEffect(EffectSlot.STATIC, new MatchingCreaturesMustAttackEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.JUGGERNAUT),
                        new PermanentControlledBySourceControllerPredicate()))));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CantBeBlockedByCreaturesMatchingPredicateEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.WALL)),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.JUGGERNAUT)));
        addEffect(EffectSlot.STATIC, new SetBasePowerToughnessEffect(5, 3, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.JUGGERNAUT, GrantScope.OWN_CREATURES));
    }
}
