package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "75")
public class DarkApostle extends Card {

    public DarkApostle() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new RegisterDelayedControllerSpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new CascadeEffect()),
                        true,
                        false)),
                "{3}, {T}: The next noncreature spell you cast this turn has cascade."
        ));
    }
}
