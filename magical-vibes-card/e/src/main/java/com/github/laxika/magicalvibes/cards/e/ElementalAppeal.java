package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostCreatedTokensEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "123")
public class ElementalAppeal extends Card {

    public ElementalAppeal() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{5}"));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Elemental",
                7,
                1,
                CardColor.RED,
                null,
                List.of(CardSubtype.ELEMENTAL),
                Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                true,
                false,
                0,
                Set.of()));
        addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new Kicked(), new BoostCreatedTokensEffect(7, 0)));
    }
}
