package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "128")
public class ArchersParapet extends Card {

    public ArchersParapet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)),
                "{1}{B}, {T}: Each opponent loses 1 life."
        ));
    }
}
