package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardRevealDiscardUnlessLandEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "146")
public class FaadiyahSeer extends Card {

    public FaadiyahSeer() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DrawCardRevealDiscardUnlessLandEffect()),
                "{T}: Draw a card and reveal it. If it isn't a land card, discard it."));
    }
}
