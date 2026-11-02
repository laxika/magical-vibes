package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WEREWOLF, CardSubtype.WOLF))));

        // At the beginning of your end step, create a 2/2 green Wolf creature token.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new CreateTokenEffect("Wolf", 2, 2,
                        CardColor.GREEN, List.of(CardSubtype.WOLF),
                        Set.of(), Set.of()));

        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Howlpack Alpha.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
