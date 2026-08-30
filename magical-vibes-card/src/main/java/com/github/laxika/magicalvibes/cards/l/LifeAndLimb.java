package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "133")
public class LifeAndLimb extends Card {

    private static final PermanentAnyOfPredicate FOREST_OR_SAPROLING = new PermanentAnyOfPredicate(List.of(
            new PermanentHasSubtypePredicate(CardSubtype.FOREST),
            new PermanentHasSubtypePredicate(CardSubtype.SAPROLING)));

    public LifeAndLimb() {
        addEffect(EffectSlot.STATIC, new AnimatePermanentsEffect(
                new Fixed(1),
                new Fixed(1),
                List.of(CardSubtype.SAPROLING, CardSubtype.FOREST), Set.of(), null,
                Set.of(CardType.LAND), GrantScope.ALL_PERMANENTS, EffectDuration.CONTINUOUS,
                FOREST_OR_SAPROLING, Set.of(CardColor.GREEN)));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.GREEN), GrantScope.ALL_PERMANENTS,
                new PermanentHasSubtypePredicate(CardSubtype.FOREST)));
    }
}
