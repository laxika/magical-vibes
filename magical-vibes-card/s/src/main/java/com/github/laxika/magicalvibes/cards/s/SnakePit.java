package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "271")
public class SnakePit extends Card {

    public SnakePit() {
        // Whenever an opponent casts a blue or black spell, you may create a 1/1 green Snake creature token.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.BLUE),
                                new CardColorPredicate(CardColor.BLACK))),
                        List.of(new CreateTokenEffect("Snake", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SNAKE), Set.of(), Set.of()))),
                "Create a 1/1 green Snake creature token?"));
    }
}
