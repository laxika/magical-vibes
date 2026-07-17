package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordToSelfEffect;

import java.util.List;

/**
 * Urza's Avenger — {6} Artifact Creature.
 * "{0}: This creature gets -1/-1 and gains your choice of banding, flying, first strike,
 * or trample until end of turn."
 * Banding is not modeled by the engine, so it is omitted from the keyword options.
 */
@CardRegistration(set = "5ED", collectorNumber = "405")
public class UrzasAvenger extends Card {

    public UrzasAvenger() {
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new BoostSelfEffect(-1, -1),
                        new GrantChosenKeywordToSelfEffect(
                                List.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.TRAMPLE))),
                "{0}: This creature gets -1/-1 and gains your choice of flying, first strike, or trample until end of turn."));
    }
}
