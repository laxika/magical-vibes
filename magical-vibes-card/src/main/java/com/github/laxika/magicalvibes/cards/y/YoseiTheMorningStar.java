package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Yosei, the Morning Star — "When Yosei dies, target player skips their next untap step. Tap up to
 * five target permanents that player controls."
 *
 * <p>One atomic trigger with one player target, so the two steps must be a {@link SequenceEffect}
 * (a trigger collector pushes a separate stack entry — and therefore a separate target choice —
 * per slot effect). Both steps read the trigger's player target: the skip is queued on that player
 * and the tap offers up to five of the permanents they control (chosen at resolution, choosing none
 * is legal).
 */
@CardRegistration(set = "CHK", collectorNumber = "50")
public class YoseiTheMorningStar extends Card {

    public YoseiTheMorningStar() {
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new SkipNextUntapStepEffect(),
                new TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS,
                        new PermanentTruePredicate(), 5)));
    }
}
