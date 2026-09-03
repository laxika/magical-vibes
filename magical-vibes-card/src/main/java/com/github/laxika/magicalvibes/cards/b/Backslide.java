package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TurnTargetCreatureFaceDownEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasMorphAbilityPredicate;

@CardRegistration(set = "ONS", collectorNumber = "70")
public class Backslide extends Card {

    public Backslide() {
        addEffect(EffectSlot.SPELL,
                new TurnTargetCreatureFaceDownEffect(new PermanentHasMorphAbilityPredicate()));
        addCycling("{U}");
    }
}
