package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "156")
public class OjutaiSoulOfWinter extends Card {

    public OjutaiSoulOfWinter() {
        // Whenever a Dragon you control attacks, tap target nonland permanent an opponent controls.
        // That permanent doesn't untap during its controller's next untap step.
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                        new TriggeringCardConditionalEffect(
                                new CardSubtypePredicate(CardSubtype.DRAGON),
                                SequenceEffect.of(
                                        new TapPermanentsEffect(TapUntapScope.TARGET),
                                        new SkipNextUntapEffect(TapUntapScope.TARGET))));
    }
}
