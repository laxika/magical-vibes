package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "57")
public class IntoTheFaeCourt extends Card {

    public IntoTheFaeCourt() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1,
                "Faerie",
                1,
                1,
                CardColor.BLUE,
                List.of(CardSubtype.FAERIE),
                Set.of(Keyword.FLYING),
                Set.of(),
                Map.of(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        "creatures with flying"
                ))
        ));
    }
}
