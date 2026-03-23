package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "95")
public class JosuVessLichKnight extends Card {

    public JosuVessLichKnight() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{5}{B}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new KickedConditionalEffect(
                new CreateTokenEffect(8, "Zombie Knight", 2, 2,
                        CardColor.BLACK, List.of(CardSubtype.ZOMBIE, CardSubtype.KNIGHT),
                        Set.of(Keyword.MENACE), Set.of())
        ));
    }
}
