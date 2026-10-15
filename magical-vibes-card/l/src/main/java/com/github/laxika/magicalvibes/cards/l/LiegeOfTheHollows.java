package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaForTokensEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "WTH", collectorNumber = "131")
public class LiegeOfTheHollows extends Card {

    public LiegeOfTheHollows() {
        addEffect(EffectSlot.ON_DEATH, new EachPlayerPaysAnyManaForTokensEffect(
                new CreateTokenEffect("Squirrel", 1, 1, CardColor.GREEN, List.of(CardSubtype.SQUIRREL),
                        Set.of(), Set.of())));
    }
}
