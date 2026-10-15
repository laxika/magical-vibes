package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "90")
public class UndeadLeotau extends Card {

    public UndeadLeotau() {
        // {R}: This creature gets +1/-1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostSelfEffect(1, -1)),
                "{R}: This creature gets +1/-1 until end of turn."
        ));

        // Unearth {2}{B}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{2}{B}");
    }
}
