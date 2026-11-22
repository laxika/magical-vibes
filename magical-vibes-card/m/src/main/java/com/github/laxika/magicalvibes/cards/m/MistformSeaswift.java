package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;
import java.util.Set;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "45")
public class MistformSeaswift extends Card {

    public MistformSeaswift() {
        addMorph("{1}{U}");
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(Subjects.source(), Set.of())),
                "{1}: This creature becomes the creature type of your choice until end of turn."
        ));
    }
}
