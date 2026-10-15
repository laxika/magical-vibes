package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;
import java.util.Set;

public class WillScholarOfFrost extends Card {

    public WillScholarOfFrost() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                instantOrSorcery, 1, CostModificationScope.SELF));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new AnimatePermanentsEffect(
                        0, 2, List.of(), Set.of(), null, Set.of(),
                        GrantScope.TARGET, EffectDuration.UNTIL_YOUR_NEXT_TURN)),
                "+1: Up to one target creature has base power and toughness 0/2 until your next turn.",
                null,
                1,
                null,
                null,
                List.of(TargetFilters.creature()),
                0,
                1));
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DrawCardEffect(2)),
                "-3: Draw two cards."));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileTargetPermanentEffect(new CreateTokenEffect(
                        "Elemental", 4, 4, CardColor.BLUE,
                        Set.of(CardColor.BLUE, CardColor.RED), List.of(CardSubtype.ELEMENTAL)))),
                "-7: Exile up to five target permanents. For each permanent exiled this way, its controller creates a 4/4 blue and red Elemental creature token.",
                null,
                -7,
                null,
                null,
                List.of(TargetFilters.permanent()),
                0,
                5));
    }
}
