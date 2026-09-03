package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "9ED", collectorNumber = "231")
@CardRegistration(set = "ONS", collectorNumber = "247")
public class Biorhythm extends Card {

    public Biorhythm() {
        // Each player's life total becomes the number of creatures they control. EACH_PLAYER
        // evaluates the amount once per player, so CountScope.CONTROLLER reads "they".
        addEffect(EffectSlot.SPELL, new SetLifeTotalEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                SetLifeTotalRecipient.EACH_PLAYER));
    }
}
