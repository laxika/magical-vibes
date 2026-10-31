package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "LRW", collectorNumber = "36")
@CardRegistration(set = "CMD", collectorNumber = "26")
public class PollenLullaby extends Card {

    public PollenLullaby() {
        // Prevent all combat damage that would be dealt this turn.
        addSpellEffect(PreventDamageEffect.allCombat());

        // Clash with an opponent. If you win, creatures that player controls don't untap during
        // the player's next untap step. (2-player: the clash opponent is the single opponent, so
        // on a won clash keep every creature not controlled by the caster tapped through its next
        // untap step.)
        addSpellEffect(new ClashEffect(
                new SkipNextUntapEffect(TapUntapScope.OPPONENTS_PERMANENTS,
                        new PermanentIsCreaturePredicate(), 1, true)));
    }
}
