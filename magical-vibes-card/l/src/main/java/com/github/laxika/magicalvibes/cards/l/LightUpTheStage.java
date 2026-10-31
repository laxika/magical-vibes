package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

@CardRegistration(set = "RNA", collectorNumber = "107")
@CardRegistration(set = "FCA", collectorNumber = "39")
@CardRegistration(set = "RVR", collectorNumber = "117")
@CardRegistration(set = "DSC", collectorNumber = "166")
@CardRegistration(set = "AFC", collectorNumber = "131")
public class LightUpTheStage extends Card {

    public LightUpTheStage() {
        addCastingOption(AlternateHandCast.spectacle("{R}", new OpponentLostLifeThisTurn(1)));
        addSpellEffect(new ExileTopCardsMayPlayUntilNextTurnEffect(2));
    }
}
