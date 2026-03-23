package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

/**
 * Howlpack Alpha — back face of Mayor of Avabruck.
 * 3/3 Werewolf.
 * Each other creature you control that's a Werewolf or a Wolf gets +1/+1.
 * At the beginning of your end step, create a 2/2 green Wolf creature token.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Howlpack Alpha.
 */
public class HowlpackAlpha extends Card {

    public HowlpackAlpha() {
        // Each other creature you control that's a Werewolf or a Wolf gets +1/+1.
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WEREWOLF, CardSubtype.WOLF))));

        // At the beginning of your end step, create a 2/2 green Wolf creature token.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new CreateTokenEffect("Wolf", 2, 2,
                        CardColor.GREEN, List.of(CardSubtype.WOLF),
                        Set.of(), Set.of()));

        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Howlpack Alpha.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new TwoOrMoreSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }
}
