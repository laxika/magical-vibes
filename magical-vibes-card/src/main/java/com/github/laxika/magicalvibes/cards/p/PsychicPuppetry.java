package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CHK", collectorNumber = "80")
public class PsychicPuppetry extends Card {

    public PsychicPuppetry() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.SPELL, new MayEffect(
                new TapOrUntapTargetPermanentEffect(),
                "Tap or untap target permanent?"
        ));
        addEffect(EffectSlot.STATIC, new SpliceEffect(CardSubtype.ARCANE, "{1}{U}"));
    }
}
