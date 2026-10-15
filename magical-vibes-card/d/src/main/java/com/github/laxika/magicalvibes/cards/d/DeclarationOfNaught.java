package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesChosenNameWithSourcePredicate;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "29")
public class DeclarationOfNaught extends Card {

    public DeclarationOfNaught() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardNameOnEnterEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new CounterSpellEffect()),
                "{U}: Counter target spell with the chosen name.",
                new StackEntryPredicateTargetFilter(
                        new StackEntrySharesChosenNameWithSourcePredicate(),
                        "Target must be a spell with the chosen name.")
        ));
    }
}
