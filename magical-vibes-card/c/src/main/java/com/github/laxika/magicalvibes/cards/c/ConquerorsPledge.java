package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "8")
public class ConquerorsPledge extends Card {

    public ConquerorsPledge() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{6}"));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(),
                new CreateTokenEffect(6, "Kor Soldier", 1, 1,
                        CardColor.WHITE, List.of(CardSubtype.KOR, CardSubtype.SOLDIER),
                        Set.of(), Set.of()),
                new CreateTokenEffect(12, "Kor Soldier", 1, 1,
                        CardColor.WHITE, List.of(CardSubtype.KOR, CardSubtype.SOLDIER),
                        Set.of(), Set.of())
        ));
    }
}
