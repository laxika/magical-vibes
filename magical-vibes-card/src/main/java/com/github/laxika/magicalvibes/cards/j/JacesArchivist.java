package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsHandThenDrawsGreatestDiscardedEffect;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "59")
public class JacesArchivist extends Card {

    public JacesArchivist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new EachPlayerDiscardsHandThenDrawsGreatestDiscardedEffect()),
                "{U}, {T}: Each player discards their hand, then draws cards equal to the greatest number of cards a player discarded this way."
        ));
    }
}
