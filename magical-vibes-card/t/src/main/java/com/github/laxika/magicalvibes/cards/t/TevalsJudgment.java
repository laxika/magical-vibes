package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "28")
@CardRegistration(set = "TDC", collectorNumber = "68")
public class TevalsJudgment extends Card {

    public TevalsJudgment() {
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD,
                new ChooseModeNotYetChosenThisTurnEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Draw a card.", new DrawCardEffect(1)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Create a Treasure token.", CreateTokenEffect.ofTreasureToken(1)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Create a 2/2 black Zombie Druid creature token.",
                                new CreateTokenEffect(1, "Zombie Druid", 2, 2, CardColor.BLACK,
                                        List.of(CardSubtype.ZOMBIE, CardSubtype.DRUID), Set.of(), Set.of()))
                )));
    }
}
