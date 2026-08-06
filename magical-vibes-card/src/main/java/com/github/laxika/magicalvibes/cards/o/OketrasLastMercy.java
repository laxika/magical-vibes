package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "HOU", collectorNumber = "18")
public class OketrasLastMercy extends Card {

    public OketrasLastMercy() {
        // Your life total becomes equal to your starting life total.
        addEffect(EffectSlot.SPELL, new SetLifeTotalEffect(GameData.STARTING_LIFE_TOTAL));

        // Lands you control don't untap during your next untap step.
        addEffect(EffectSlot.SPELL, new SkipNextUntapEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));
    }
}
