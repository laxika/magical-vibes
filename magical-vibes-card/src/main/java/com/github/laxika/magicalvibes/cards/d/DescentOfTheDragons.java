package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForEachDestroyedPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "133")
public class DescentOfTheDragons extends Card {

    public DescentOfTheDragons() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Targets must be creatures"
        ), 0, 99).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
        addEffect(EffectSlot.SPELL,
                new CreateTokenForEachDestroyedPermanentControllerEffect(
                        new CreateTokenEffect("Dragon", 4, 4, CardColor.RED,
                                List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of())));
    }
}
