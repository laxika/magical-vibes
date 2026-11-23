package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsUnblockedAttackingPredicate;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "168")
@CardRegistration(set = "PC2", collectorNumber = "97")
@CardRegistration(set = "PCA", collectorNumber = "97")
@CardRegistration(set = "NCC", collectorNumber = "342")
public class InkfathomWitch extends Card {

    public InkfathomWitch() {
        // Fear is auto-loaded from Scryfall keywords.
        // {2}{U}{B}: Each unblocked creature has base power and toughness 4/1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{B}",
                List.of(new SetBasePowerToughnessEffect(4, 1, Subjects.allCreatures(),
                        Duration.UNTIL_END_OF_TURN, new PermanentIsUnblockedAttackingPredicate())),
                "{2}{U}{B}: Each unblocked creature has base power and toughness 4/1 until end of turn."
        ));
    }
}
