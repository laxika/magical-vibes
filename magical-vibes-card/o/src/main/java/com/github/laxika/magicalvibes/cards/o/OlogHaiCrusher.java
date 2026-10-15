package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "200")
public class OlogHaiCrusher extends Card {

    public OlogHaiCrusher() {
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new AnyOf(List.of(
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ORC))
                )),
                "you control a Goblin or Orc"
        ));
    }
}
