package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

@CardRegistration(set = "RIX", collectorNumber = "24")
public class SphinxsDecree extends Card {

    public SphinxsDecree() {
        addSpellEffect(new PlayerRestrictionEffect(Subjects.eachOpponent(),
                PlayerRestriction.castingSpellsOf(CardType.INSTANT, CardType.SORCERY),
                Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN));
    }
}
