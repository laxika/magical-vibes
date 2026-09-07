package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "12")
public class FinaleOfGlory extends Card {

    public FinaleOfGlory() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new XValue(), "Soldier", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.SOLDIER), Set.of(Keyword.VIGILANCE), Set.of()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SpellXAtLeast(10),
                new CreateTokenEffect(new XValue(), "Angel", 4, 4, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of())));
    }
}
