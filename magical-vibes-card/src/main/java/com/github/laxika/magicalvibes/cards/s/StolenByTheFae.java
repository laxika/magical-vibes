package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "66")
public class StolenByTheFae extends Card {

    public StolenByTheFae() {
        var creatureWithManaValueX = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentManaValueEqualsXPredicate()));
        target(new PermanentPredicateTargetFilter(
                creatureWithManaValueX,
                "Target must be a creature with mana value X."
        )).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target(creatureWithManaValueX));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new XValue(), "Faerie", 1, 1, CardColor.BLUE,
                List.of(CardSubtype.FAERIE), Set.of(Keyword.FLYING), Set.of()));
    }
}
