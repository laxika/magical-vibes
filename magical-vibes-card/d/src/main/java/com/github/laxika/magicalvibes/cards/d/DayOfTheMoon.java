package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnSnapshotEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenNamePredicate;

@CardRegistration(set = "WHO", collectorNumber = "79")
@CardRegistration(set = "WHO", collectorNumber = "684")
public class DayOfTheMoon extends Card {

    public DayOfTheMoon() {
        for (EffectSlot chapter : new EffectSlot[]{
                EffectSlot.SAGA_CHAPTER_I, EffectSlot.SAGA_CHAPTER_II, EffectSlot.SAGA_CHAPTER_III}) {
            addEffect(chapter, new ChooseCardNameAtResolutionEffect(CardType.CREATURE));
            addEffect(chapter, new GoadCreaturesUntilNextTurnSnapshotEffect(
                    new PermanentHasSourceChosenNamePredicate(true)));
        }
    }
}
