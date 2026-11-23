package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

@CardRegistration(set = "BRO", collectorNumber = "4")
public class CalamitysWake extends Card {

    public CalamitysWake() {
        addSpellEffect(new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS));
        addSpellEffect(PlayerRestrictionEffect.thisTurn(Subjects.eachPlayer(),
                PlayerRestriction.castingNoncreatureSpells()));
        addSpellEffect(new ExileSpellEffect());
    }
}
