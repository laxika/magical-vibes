package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatIfLastNonlandCreateTokensEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "110")
public class RallyTheHorde extends Card {

    public RallyTheHorde() {
        addEffect(EffectSlot.SPELL, new ExileTopCardsRepeatIfLastNonlandCreateTokensEffect(
                3,
                new CreateTokenEffect(1, "Warrior", 1, 1, CardColor.RED,
                        List.of(CardSubtype.WARRIOR), Set.of(), Set.of())));
    }
}
