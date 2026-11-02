package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "314")
public class ExperimentalConfectioner extends Card {

    public ExperimentalConfectioner() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofFoodToken(1));
        addTrigger(Triggers.sacrificed(new PermanentHasSubtypePredicate(CardSubtype.FOOD), ObjectRelation.YOU_CONTROL),
                ratToken());
    }

    private static CreateTokenEffect ratToken() {
        return new CreateTokenEffect(
                1, "Rat", 1, 1, CardColor.BLACK, List.of(CardSubtype.RAT),
                Set.of(), Set.of(), List.of(StaticAbility.of(new CantBlockEffect())));
    }
}
