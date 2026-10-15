package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "75")
public class HeadOfTheHunt extends Card {

    public HeadOfTheHunt() {
        addEffect(EffectSlot.STATIC, new ExileOpponentCreaturesInsteadOfDyingEffect(
                false,
                new CreateTokenEffect("Wolf", 2, 2, CardColor.GREEN,
                        List.of(CardSubtype.WOLF), Set.of(), Set.of())));
    }
}
