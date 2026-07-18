package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardRevealDiscardUnlessLandEffect;
import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "100")
public class Sindbad extends Card {

    public Sindbad() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DrawCardRevealDiscardUnlessLandEffect()),
                "{T}: Draw a card and reveal it. If it isn't a land card, discard it."));
    }
}
