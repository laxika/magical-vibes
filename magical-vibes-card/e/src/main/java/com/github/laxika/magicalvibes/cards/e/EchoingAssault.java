package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "24")
@CardRegistration(set = "BLC", collectorNumber = "58")
public class EchoingAssault extends Card {

    public EchoingAssault() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.OWN_CREATURES,
                        new PermanentIsTokenPredicate()));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate(),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        new PermanentIsAttackingDefendingPlayerPredicate()
                )),
                "Target must be a nontoken creature attacking that player"
        )).addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(), Set.of(), 1, 1, Map.of(),
                        false, false, true, true));
    }
}
