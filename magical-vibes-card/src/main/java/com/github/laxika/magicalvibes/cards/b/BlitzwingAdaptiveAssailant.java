package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantRandomKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TransformToFrontFaceEffect;

import java.util.List;

/** Back face of Blitzwing, Cruel Tormentor. */
public class BlitzwingAdaptiveAssailant extends Card {

    public BlitzwingAdaptiveAssailant() {
        // Living metal: during your turn, this Vehicle is also a creature.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new NotControllerTurn()),
                new GrantCardTypeEffect(CardType.CREATURE, GrantScope.SELF)));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new GrantRandomKeywordEffect(List.of(Keyword.FLYING, Keyword.INDESTRUCTIBLE), GrantScope.SELF));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new TransformToFrontFaceEffect());
    }
}
