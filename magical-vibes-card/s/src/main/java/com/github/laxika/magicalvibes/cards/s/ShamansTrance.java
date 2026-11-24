package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowPlayFromOtherGraveyardsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "JUD", collectorNumber = "98")
public class ShamansTrance extends Card {

    public ShamansTrance() {
        addSpellEffect(PlayerRestrictionEffect.thisTurn(Subjects.eachOpponent(),
                PlayerRestriction.playingCardsFrom(Zone.GRAVEYARD)));
        addSpellEffect(new AllowPlayFromOtherGraveyardsThisTurnEffect());
    }
}
