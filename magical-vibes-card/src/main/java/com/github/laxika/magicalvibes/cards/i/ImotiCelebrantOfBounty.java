package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

import java.util.List;

@CardRegistration(set = "MUL", collectorNumber = "43")
@CardRegistration(set = "MUL", collectorNumber = "108")
@CardRegistration(set = "MUL", collectorNumber = "173")
public class ImotiCelebrantOfBounty extends Card {

    public ImotiCelebrantOfBounty() {
        // Cascade when Imoti is cast.
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());

        // Spells with mana value 6 or greater that its controller casts have cascade. The generic
        // spell-cast collector snapshots the triggering spell's mana value for CascadeEffect.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardMinManaValuePredicate(6, true), List.of(new CascadeEffect())));
    }
}
