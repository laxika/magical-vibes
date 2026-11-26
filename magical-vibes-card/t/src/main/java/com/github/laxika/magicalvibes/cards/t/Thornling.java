package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "95")
public class Thornling extends Card {

    public Thornling() {
        // {G}: This creature gains haste until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new GrantKeywordEffect(Keyword.HASTE, Subjects.source())),
                "{G}: Thornling gains haste until end of turn."));

        // {G}: This creature gains trample until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.source())),
                "{G}: Thornling gains trample until end of turn."));

        // {G}: This creature gains indestructible until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source())),
                "{G}: Thornling gains indestructible until end of turn."));

        // {1}: This creature gets +1/-1 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(BoostEffect.self(1, -1)),
                "{1}: Thornling gets +1/-1 until end of turn."));

        // {1}: This creature gets -1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(BoostEffect.self(-1, 1)),
                "{1}: Thornling gets -1/+1 until end of turn."));
    }
}
