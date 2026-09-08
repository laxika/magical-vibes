package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.CastForForetellCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "33")
public class StarnheimUnleashed extends Card {

    public StarnheimUnleashed() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new CastForForetellCost(),
                angelWarriors(1),
                angelWarriors(new XValue())
        ));
        addCastingOption(new ForetellCast("{X}{X}{W}"));
    }

    private static CreateTokenEffect angelWarriors(int amount) {
        return new CreateTokenEffect(amount, "Angel Warrior", 4, 4, CardColor.WHITE,
                List.of(CardSubtype.ANGEL, CardSubtype.WARRIOR),
                Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of());
    }

    private static CreateTokenEffect angelWarriors(XValue amount) {
        return new CreateTokenEffect(amount, "Angel Warrior", 4, 4, CardColor.WHITE,
                List.of(CardSubtype.ANGEL, CardSubtype.WARRIOR),
                Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of());
    }
}
