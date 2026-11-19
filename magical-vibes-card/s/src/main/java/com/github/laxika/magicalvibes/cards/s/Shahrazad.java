package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedUpEffect;
import com.github.laxika.magicalvibes.model.effect.StartSubgameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ARN", collectorNumber = "10")
public class Shahrazad extends Card {
    public Shahrazad() {
        addSpellEffect(new StartSubgameEffect());
        addSpellEffect(new EachPlayerLosesFractionOfLifeRoundedUpEffect(
                2, Subjects.except(Subjects.eachPlayer(), Subjects.subgameWinners())));
    }
}
