package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "131")
public class VectisAgents extends Card {

    public VectisAgents() {
        // {U}{B}: This creature gets -2/-0 until end of turn and can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{B}",
                List.of(new BoostSelfEffect(-2, 0), new MakeCreatureUnblockableEffect(true)),
                "{U}{B}: Vectis Agents gets -2/-0 until end of turn and can't be blocked this turn."
        ));
    }
}
