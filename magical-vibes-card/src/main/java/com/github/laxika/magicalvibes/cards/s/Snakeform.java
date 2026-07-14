package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "EVE", collectorNumber = "161")
public class Snakeform extends Card {

    public Snakeform() {
        // Until end of turn, target creature loses all abilities and becomes a green
        // Snake with base power and toughness 1/1.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.SPELL, new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN))
                .addEffect(EffectSlot.SPELL, new GrantColorUntilEndOfTurnEffect(CardColor.GREEN))
                .addEffect(EffectSlot.SPELL, new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.SNAKE))
                .addEffect(EffectSlot.SPELL, new SetBasePowerToughnessEffect(1, 1))
                // Draw a card.
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
