package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.JeweledBirdAnteEffect;
import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "102")
public class JeweledBird extends Card {

    public JeweledBird() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new JeweledBirdAnteEffect()),
                "{T}: Ante this artifact. If you do, put all other cards you own from the ante into your graveyard, then draw a card."
        ));
    }
}
