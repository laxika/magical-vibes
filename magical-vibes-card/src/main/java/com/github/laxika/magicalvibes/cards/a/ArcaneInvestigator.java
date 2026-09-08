package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "46")
public class ArcaneInvestigator extends Card {

    public ArcaneInvestigator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}",
                List.of(new RollD20Effect(
                        new DrawCardEffect(1),
                        LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)))),
                "Search the Room — {5}{U}: Roll a d20. 1–9: Draw a card. 10–20: Look at the top three cards of your library. Put one of them into your hand and the rest on the bottom of your library in any order."
        ));
    }
}
