package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileArtifactFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "195")
public class PrototypePortal extends Card {

    public PrototypePortal() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExileArtifactFromHandToImprintEffect(), "You may exile an artifact card from your hand."));
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new CreateTokenCopyOfImprintedCardEffect(false, false)),
                "{X}, {T}: Create a token that's a copy of the exiled card. X is the mana value of that card."));
    }
}
