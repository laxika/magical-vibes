package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "175")
public class MimicVat extends Card {

    public MimicVat() {
        addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                new MayEffect(new ImprintDyingCreatureEffect(), "You may exile that card."));
        addActivatedAbility(new ActivatedAbility(true, "{3}",
                List.of(new CreateTokenCopyOfImprintedCardEffect()),
                "{3}, {T}: Create a token that's a copy of a card exiled with Mimic Vat. It gains haste. Exile it at the beginning of the next end step."));
    }
}
