package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "4")
public class BeskirShieldmate extends Card {

    public BeskirShieldmate() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                1, "Human Warrior", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.WARRIOR), Set.of(), Set.of()));
    }
}
