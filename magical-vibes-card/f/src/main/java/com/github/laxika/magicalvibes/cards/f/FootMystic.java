package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "63")
public class FootMystic extends Card {

    public FootMystic() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new PermanentLeftBattlefieldUnderYourControlThisTurn(),
                new CreateTokenEffect(
                        "Ninja", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.NINJA), Set.of(), Set.of())));
    }
}
