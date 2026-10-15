package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardAndRemoveLoyaltyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "63")
public class JaceMirrorMage extends Card {

    public JaceMirrorMage() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(),
                CreateTokenCopyOfSourceEffect.withStartingLoyalty(true, 1, 1)));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new ScryEffect(2)),
                "+1: Scry 2."
        ));
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new DrawCardEffect(1), new RevealDrawnCardAndRemoveLoyaltyCountersEffect()),
                "0: Draw a card and reveal it. Remove a number of loyalty counters equal to that card's mana value from Jace."
        ));
    }
}
