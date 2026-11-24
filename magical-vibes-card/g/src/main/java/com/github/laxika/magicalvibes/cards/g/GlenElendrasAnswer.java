package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CounterMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "52")
@CardRegistration(set = "ECL", collectorNumber = "306")
public class GlenElendrasAnswer extends Card {

    public GlenElendrasAnswer() {
        addStaticEffect(new CantBeCounteredEffect());
        addSpellEffect(CounterMatchingSpellsEffect.spellsAndAbilities(
                new StackEntryNotPredicate(new StackEntryControlledByPredicate())));
        addSpellEffect(new CreateTokenEffect(1, "Faerie", 1, 1, CardColor.BLUE,
                Set.of(CardColor.BLUE, CardColor.BLACK), List.of(CardSubtype.FAERIE),
                Set.of(Keyword.FLYING), Set.of()).withAmount(new EventValue()));
    }
}
