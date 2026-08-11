package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentPutThreeOrMoreCardsIntoGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "109")
public class RavenousTrap extends Card {

    public RavenousTrap() {
        addCastingOption(new AlternateHandCast(
                List.of(),
                new OpponentPutThreeOrMoreCardsIntoGraveyardThisTurn(),
                false));
        addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE));
    }
}
