package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "237")
public class MonumentToEndurance extends Card {

    public MonumentToEndurance() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new ChooseModeNotYetChosenThisTurnEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Create a Treasure token", CreateTokenEffect.ofTreasureToken(1)),
                new ChooseOneEffect.ChooseOneOption("Each opponent loses 3 life",
                        new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT)))));
    }
}
