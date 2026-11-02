package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "223")
@CardRegistration(set = "TDC", collectorNumber = "307")
@CardRegistration(set = "OTC", collectorNumber = "244")
public class ThirdPathIconoclast extends Card {

    public ThirdPathIconoclast() {
        // Whenever you cast a noncreature spell, create a 1/1 colorless Soldier artifact creature token.
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new CreateTokenEffect("Soldier", 1, 1, null,
                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of(CardType.ARTIFACT)))
        ));
    }
}
