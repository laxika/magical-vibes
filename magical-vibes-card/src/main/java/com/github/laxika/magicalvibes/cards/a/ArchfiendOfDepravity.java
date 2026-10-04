package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayerChoosesUpToPermanentsThenSacrificesRestEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "FRF", collectorNumber = "62")
@CardRegistration(set = "FIC", collectorNumber = "273")
@CardRegistration(set = "SCD", collectorNumber = "67")
@CardRegistration(set = "E01", collectorNumber = "31")
public class ArchfiendOfDepravity extends Card {

    public ArchfiendOfDepravity() {
        addEffect(EffectSlot.OPPONENT_END_STEP_TRIGGERED,
                new PlayerChoosesUpToPermanentsThenSacrificesRestEffect(
                        2, new PermanentIsCreaturePredicate()));
    }
}
