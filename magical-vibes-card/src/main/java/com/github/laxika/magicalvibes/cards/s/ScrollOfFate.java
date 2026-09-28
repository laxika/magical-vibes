package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ManifestCardFromHandEffect;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "251")
@CardRegistration(set = "MKC", collectorNumber = "235")
public class ScrollOfFate extends Card {

    public ScrollOfFate() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new ManifestCardFromHandEffect()),
                "{T}: Manifest a card from your hand."));
    }
}
