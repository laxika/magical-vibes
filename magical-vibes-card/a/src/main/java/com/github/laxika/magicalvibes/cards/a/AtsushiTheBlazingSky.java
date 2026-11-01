package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "134")
@CardRegistration(set = "SOC", collectorNumber = "236")
@CardRegistration(set = "TDC", collectorNumber = "204")
@CardRegistration(set = "FDC", collectorNumber = "138")
public class AtsushiTheBlazingSky extends Card {

    public AtsushiTheBlazingSky() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top two cards of your library. Until the end of your next turn, you may play those cards.",
                        new ExileTopCardsMayPlayUntilNextTurnEffect(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create three Treasure tokens",
                        CreateTokenEffect.ofTreasureToken(3))
        )));
    }
}
