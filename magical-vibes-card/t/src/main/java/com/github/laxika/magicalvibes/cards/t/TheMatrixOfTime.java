package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsExiledWithPlanarSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "589")
public class TheMatrixOfTime extends Card {

    public TheMatrixOfTime() {
        addTrigger(Triggers.planeswalkToThis(),
                new ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect());
        addStaticEffect(new AllowPlayCardsExiledWithPlanarSourceEffect());

        // Whenever you play a land or cast a spell from among cards exiled with The Matrix of Time, that card's
        // owner loses 3 life and exiles the top card of their library.
        CardEffect playedCardFollowUp = SequenceEffect.of(
                new LoseLifeEffect(3, Subjects.ownerOf(Subjects.triggeringCard())),
                ExileTopCardOfOwnLibraryEffect.of(Subjects.ownerOf(Subjects.triggeringCard())));
        addTrigger(Triggers.youCastSpell().exiledWithThis(), playedCardFollowUp);
        addTrigger(Triggers.youPlayLand().exiledWithThis(), playedCardFollowUp);
        addTrigger(Triggers.chaosEnsues(), CreateTokenEffect.ofTreasureToken(2));
    }
}
