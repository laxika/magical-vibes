package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "699")
public class MoltenLavamancer extends Card {

    public MoltenLavamancer() {
        addTrigger(Triggers.sourceYouControlDealsNoncombatDamageToOpponent(),
                new OncePerTurnTriggerEffect(new ConditionalEffect(new ControllerTurn(),
                        new CreateTokenEffect(1, "Elemental", 1, 1, CardColor.RED,
                                List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of()))));
    }
}
