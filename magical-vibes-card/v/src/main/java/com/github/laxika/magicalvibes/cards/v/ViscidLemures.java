package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "141")
public class ViscidLemures extends Card {

    public ViscidLemures() {
        // {0}: This creature gets -1/-0 and gains swampwalk until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(BoostEffect.self(-1, 0), new GrantKeywordEffect(Keyword.SWAMPWALK, Subjects.source())),
                "{0}: This creature gets -1/-0 and gains swampwalk until end of turn."));
    }
}
