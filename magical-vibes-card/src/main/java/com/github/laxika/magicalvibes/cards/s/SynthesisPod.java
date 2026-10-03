package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileInstantOrSorcerySpellCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerUntilManaValueAndCastEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ONC", collectorNumber = "23")
@CardRegistration(set = "ONC", collectorNumber = "33")
public class SynthesisPod extends Card {

    public SynthesisPod() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U/P}",
                List.of(
                        ExileInstantOrSorcerySpellCost.anySpellCost(),
                        new ExileTopCardsOfTargetPlayerUntilManaValueAndCastEffect()),
                "{1}{U/P}, {T}, Exile a spell you control: Target opponent reveals cards from the top of their library until they reveal a card with mana value equal to 1 plus the exiled spell's mana value. Exile that card, then that player shuffles. You may cast that exiled card without paying its mana cost.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "You must target an opponent.")));
    }
}
