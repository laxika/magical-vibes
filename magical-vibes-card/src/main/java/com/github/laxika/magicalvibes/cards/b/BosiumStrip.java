package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "147")
public class BosiumStrip extends Card {

    public BosiumStrip() {
        // {3}, {T}: Until end of turn, you may cast instant and sorcery spells from the top of your
        // graveyard. If a spell cast this way would be put into a graveyard, exile it instead.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffect()),
                "{3}, {T}: Until end of turn, you may cast instant and sorcery spells from the top "
                        + "of your graveyard. If a spell cast this way would be put into a graveyard, "
                        + "exile it instead."));
    }
}
