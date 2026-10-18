package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastThreeOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SecretMissionEffect;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "267")
@CardRegistration(set = "MB2", collectorNumber = "503")
public class MarchesasSurpriseParty extends Card {

    public MarchesasSurpriseParty() {
        addEffect(EffectSlot.COMMAND_ZONE_STATIC, new SecretMissionEffect(List.of(
                new SecretMissionEffect.Mission("You've cast three or more spells this turn.",
                        new ControllerCastThreeOrMoreSpellsThisTurn(null)),
                new SecretMissionEffect.Mission("An opponent lost 6 or more life this turn.",
                        new OpponentLostLifeThisTurn(6)),
                new SecretMissionEffect.Mission("There are nine or more cards in your graveyard.",
                        new GraveyardCardThreshold(9, null))),
                new DrawCardEffect(1)));
    }
}
