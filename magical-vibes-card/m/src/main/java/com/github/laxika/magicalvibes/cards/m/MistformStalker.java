package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;
import java.util.Set;
import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "98")
public class MistformStalker extends Card {

    public MistformStalker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(Subjects.source(), Set.of())),
                "{1}: This creature becomes the creature type of your choice until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{U}",
                List.of(
                        BoostEffect.self(2, 2),
                        new GrantKeywordEffect(Keyword.FLYING, Subjects.source())
                ),
                "{2}{U}{U}: This creature gets +2/+2 and gains flying until end of turn."
        ));
    }
}
