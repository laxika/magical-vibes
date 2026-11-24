package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFreeCastForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenCardTypePredicate;

import java.util.List;

public class ApexObservatory extends Card {

    public ApexObservatory() {
        addStaticEffect(new EntersTappedEffect());
        addStaticEffect(ChooseCardTypeOnEnterEffect.forSharedCraftMaterials());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantFreeCastForNextMatchingSpellEffect(new CardHasSourceChosenCardTypePredicate())),
                "{T}: The next spell you cast this turn of the chosen type can be cast without paying its mana cost."));
    }
}
