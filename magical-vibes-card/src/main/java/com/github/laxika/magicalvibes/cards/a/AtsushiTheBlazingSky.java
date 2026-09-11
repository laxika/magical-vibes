package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "134")
public class AtsushiTheBlazingSky extends Card {

    public AtsushiTheBlazingSky() {
        addEffect(EffectSlot.ON_DEATH, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top two cards of your library. Until the end of your next turn, you may play those cards.",
                        new ExileTopCardsMayPlayUntilNextTurnEffect(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create three Treasure tokens",
                        CreateTokenEffect.ofTreasureToken(3))
        )));
    }
}
