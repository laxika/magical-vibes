package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "131")
public class JeditOjanenOfEfrava extends Card {

    public JeditOjanenOfEfrava() {
        CreateTokenEffect catWarriorToken = new CreateTokenEffect(
                1, "Cat Warrior", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.CAT, CardSubtype.WARRIOR),
                Set.of(Keyword.FORESTWALK), Set.of()
        );
        addEffect(EffectSlot.ON_ATTACK, catWarriorToken);
        addEffect(EffectSlot.ON_BLOCK, catWarriorToken);
    }
}
