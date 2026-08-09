package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "85")
@CardRegistration(set = "MRD", collectorNumber = "57")
public class BarterInBlood extends Card {

    public BarterInBlood() {
        // Each player sacrifices two creatures of their choice. The creature filter is wrapped (not
        // bare) so it routes through the multi-permanent choice — the single-creature "sacrifice a
        // creature" primitive can only take one. EACH_PLAYER uses the APNAP simultaneous queue.
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                2,
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                SacrificeRecipient.EACH_PLAYER));
    }
}
