package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "84")
public class EscapeArtist extends Card {

    public EscapeArtist() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new DiscardCardTypeCost(null, null), ReturnToHandEffect.self()),
                "{U}, Discard a card: Return this creature to its owner's hand."
        ));
    }
}
