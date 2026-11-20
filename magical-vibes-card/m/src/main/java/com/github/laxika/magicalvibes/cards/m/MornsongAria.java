package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCannotDrawCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "111")
@CardRegistration(set = "ECL", collectorNumber = "361")
public class MornsongAria extends Card {

    public MornsongAria() {
        // Players can't draw cards or gain life.
        addStaticEffect(new PlayersCannotDrawCardsEffect());
        addStaticEffect(new PlayersCantGainLifeEffect());

        // At the beginning of each player's draw step, that player loses 3 life, searches their
        // library for a card, puts it into their hand, then shuffles.
        addTrigger(Triggers.drawStep(ActivePlayer.ANY), SequenceEffect.of(
                new LoseLifeEffect(3, Subjects.triggeringPlayer()),
                new SearchLibraryEffect().searchedBy(Subjects.triggeringPlayer())));
    }
}
