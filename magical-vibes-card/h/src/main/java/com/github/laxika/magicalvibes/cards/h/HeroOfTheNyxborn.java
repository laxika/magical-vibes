package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "219")
public class HeroOfTheNyxborn extends Card {

    public HeroOfTheNyxborn() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                1, "Human Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()
        ));

        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryTargetsSourcePredicate()),
                new BoostAllOwnCreaturesEffect(1, 0));
    }
}
