package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "210")
public class EyesOfTheWisent extends Card {

    public EyesOfTheWisent() {
        // Whenever an opponent casts a blue spell during your turn, you may create a 4/4 green
        // Elemental creature token.
        addTrigger(Triggers.opponentCastsSpell(), new MayEffect(
                SpellCastTriggerEffect.duringYourTurn(
                        new CardColorPredicate(CardColor.BLUE),
                        List.of(new CreateTokenEffect("Elemental", 4, 4, CardColor.GREEN,
                                List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of()))),
                "Create a 4/4 green Elemental creature token?"
        ));
    }
}
