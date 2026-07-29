package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfKeywordIndefinitelyEffect;

import java.util.List;

/**
 * Mist Dragon — {4}{U}{U} Creature — Dragon 4/4.
 * "{0}: This creature gains flying. (This effect lasts indefinitely.)"
 * "{0}: This creature loses flying. (This effect lasts indefinitely.)"
 * "{3}{U}{U}: This creature phases out."
 */
@CardRegistration(set = "MIR", collectorNumber = "79")
public class MistDragon extends Card {

    public MistDragon() {
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new SetSelfKeywordIndefinitelyEffect(Keyword.FLYING, true)),
                "{0}: This creature gains flying."));
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new SetSelfKeywordIndefinitelyEffect(Keyword.FLYING, false)),
                "{0}: This creature loses flying."));
        addActivatedAbility(new ActivatedAbility(false, "{3}{U}{U}",
                List.of(new PhaseOutSelfEffect()),
                "{3}{U}{U}: This creature phases out."));
    }
}
