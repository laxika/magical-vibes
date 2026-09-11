package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttackingEffect;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "12")
@CardRegistration(set = "TMT", collectorNumber = "223")
public class TheLastRoninsTechnique extends Card {

    public TheLastRoninsTechnique() {
        addSneak("{1}{W}");

        CreateTokenEffect regularTokens = new CreateTokenEffect(
                3, "Ninja Turtle Spirit", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.NINJA, CardSubtype.TURTLE, CardSubtype.SPIRIT), false);
        CreateTokenEffect attackingTokens = new CreateTokenEffect(
                1, "Ninja Turtle Spirit", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.NINJA, CardSubtype.TURTLE, CardSubtype.SPIRIT), true);

        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()), regularTokens));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForAlternateCost(), new CreateTokensAttackingEffect(3, attackingTokens)));
    }
}
