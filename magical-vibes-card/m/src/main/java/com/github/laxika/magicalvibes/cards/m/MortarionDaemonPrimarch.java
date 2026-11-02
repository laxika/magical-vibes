package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaCreateXTokensEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "41")
public class MortarionDaemonPrimarch extends Card {

    public MortarionDaemonPrimarch() {
        CreateTokenEffect astartesWarrior = new CreateTokenEffect(
                "Astartes Warrior", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ASTARTES, CardSubtype.WARRIOR),
                Set.of(Keyword.MENACE), Set.of());

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new PayXManaCreateXTokensEffect(new LifeLostThisTurn(CountScope.CONTROLLER), astartesWarrior));
    }
}
