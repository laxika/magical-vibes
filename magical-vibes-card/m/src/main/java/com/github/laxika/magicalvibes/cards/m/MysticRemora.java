package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "87")
@CardRegistration(set = "ME1", collectorNumber = "42")
@CardRegistration(set = "SLD", collectorNumber = "406")
@CardRegistration(set = "SLD", collectorNumber = "1842")
@CardRegistration(set = "DMR", collectorNumber = "59")
@CardRegistration(set = "TLE", collectorNumber = "16")
public class MysticRemora extends Card {

    public MysticRemora() {
        // Cumulative upkeep {1}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{1}"));

        // Whenever an opponent casts a noncreature spell, you may draw a card unless that player pays {4}.
        addTrigger(Triggers.opponentCastsSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new DrawCardUnlessPaysEffect(1, 4));
    }
}
