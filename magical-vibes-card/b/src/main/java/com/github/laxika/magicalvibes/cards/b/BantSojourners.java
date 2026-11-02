package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "125")
public class BantSojourners extends Card {

    public BantSojourners() {
        // When this creature dies, you may create a 1/1 white Soldier creature token.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(CreateTokenEffect.whiteSoldier(1),
                "Create a 1/1 white Soldier creature token?"));

        // Cycling {2}{W} ({2}{W}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addTrigger(Triggers.youCycleThisCard(), new MayEffect(CreateTokenEffect.whiteSoldier(1),
                "Create a 1/1 white Soldier creature token?"));
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(false, "{2}{W}",
                List.of(new DrawCardEffect(1)),
                "Cycling {2}{W} ({2}{W}, Discard this card: Draw a card.)"));
    }
}
