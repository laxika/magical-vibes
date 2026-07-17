package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToNTargetPermanentsToHandEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "54")
public class ResoundingWave extends Card {

    public ResoundingWave() {
        // Return target permanent to its owner's hand. (ReturnToHandEffect.target()'s TargetSpec
        // narrows the legal target to a battlefield permanent — no explicit target filter needed.)
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

        // Cycling {5}{W}{U}{B} ({5}{W}{U}{B}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, return two target permanents to their owners' hands." The reflexive
        // trigger rides on the cycling ability: the bounce choice (a resolution-time multi-select over
        // every permanent) resolves first, then the cycling draw resumes.
        addHandActivatedAbility(new ActivatedAbility(false, "{5}{W}{U}{B}",
                List.of(new ReturnUpToNTargetPermanentsToHandEffect(2), new DrawCardEffect(1)),
                "Cycling {5}{W}{U}{B} ({5}{W}{U}{B}, Discard this card: Draw a card.)"));
    }
}
