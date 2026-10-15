package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "184")
public class DinOfTheFireherd extends Card {

    public DinOfTheFireherd() {
        // Create a 5/5 black and red Elemental creature token. Resolved first, so the token counts
        // itself toward both the black- and red-creature counts below.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Elemental", 5, 5, CardColor.BLACK,
                Set.of(CardColor.BLACK, CardColor.RED), List.of(CardSubtype.ELEMENTAL)));

        // Target opponent sacrifices a creature of their choice for each black creature you control,
        // then sacrifices a land of their choice for each red creature you control. Both counts are
        // wrapped predicates (not bare) so they route through the multi-permanent choice rather than
        // the single-creature "sacrifice a creature" primitive.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                        new PermanentCount(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.BLACK)))),
                                CountScope.CONTROLLER),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                        SacrificeRecipient.TARGET_PLAYER))
                .addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                        new PermanentCount(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.RED)))),
                                CountScope.CONTROLLER),
                        new PermanentIsLandPredicate(),
                        SacrificeRecipient.TARGET_PLAYER));
    }
}
