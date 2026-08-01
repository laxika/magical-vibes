package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "145")
public class AzoriusCharm extends Card {

    public AzoriusCharm() {
        // Choose one —
        // • Creatures you control gain lifelink until end of turn.
        // • Draw a card.
        // • Put target attacking or blocking creature on top of its owner's library.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control gain lifelink until end of turn",
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.OWN_CREATURES)),
                new ChooseOneEffect.ChooseOneOption(
                        "Draw a card",
                        new DrawCardEffect(1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put target attacking or blocking creature on top of its owner's library",
                        new PutTargetOnTopOfLibraryEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentIsAttackingPredicate(),
                                                new PermanentIsBlockingPredicate())))),
                                "Target must be an attacking or blocking creature."))
        )));
    }
}
