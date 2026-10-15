package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "17")
@CardRegistration(set = "2XM", collectorNumber = "18")
@CardRegistration(set = "KLR", collectorNumber = "21")
public class GlintSleeveArtisan extends Card {

    public GlintSleeveArtisan() {
        // Fabricate 1 (CR 702.123a): the choice is made as the trigger resolves.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new PutCountersOnSourceEffect(1, 1, 1),
                "Put a +1/+1 counter on Glint-Sleeve Artisan?",
                new CreateTokenEffect(1, "Servo", 1, 1, null,
                        List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT))));
    }
}
