package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "79")
public class SlaughterDrone extends Card {

    public SlaughterDrone() {
        addActivatedAbility(new ActivatedAbility(false, "{C}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, Subjects.source())),
                "{C}: This creature gains deathtouch until end of turn."));
    }
}
