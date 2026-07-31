package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

/**
 * Urza's Engine — {5} Artifact Creature.
 * "Trample
 * {3}: This creature gains banding until end of turn.
 * {3}: Attacking creatures banded with this creature gain trample until end of turn."
 */
@CardRegistration(set = "ALL", collectorNumber = "135")
public class UrzasEngine extends Card {

    public UrzasEngine() {
        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new GrantKeywordEffect(Keyword.BANDING, GrantScope.SELF)),
                "{3}: This creature gains banding until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.BANDED_WITH_SELF)),
                "{3}: Attacking creatures banded with this creature gain trample until end of turn."));
    }
}
