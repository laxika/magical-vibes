package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "5")
@CardRegistration(set = "AFC", collectorNumber = "274")
public class FeySteed extends Card {

    public FeySteed() {
        PermanentPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        target(new PermanentPredicateTargetFilter(
                targetFilter, "Target must be another attacking creature you control"))
                .addEffect(EffectSlot.ON_ATTACK,
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
