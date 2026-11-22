package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LGN", collectorNumber = "48")
public class MistformWakecaster extends Card {

    public MistformWakecaster() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(Subjects.source(), Set.of())),
                "{1}: This creature becomes the creature type of your choice until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}{U}",
                List.of(new TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(Subjects.creaturesYouControl(), Set.of())),
                "{2}{U}{U}, {T}: Choose a creature type. Each creature you control becomes that type until end of turn."
        ));
    }
}
