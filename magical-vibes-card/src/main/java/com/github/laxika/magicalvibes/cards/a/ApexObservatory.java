package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFreeCastForNextSpellOfChosenCardTypeEffect;

import java.util.List;

public class ApexObservatory extends Card {

    public ApexObservatory() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                ChooseCardTypeOnEnterEffect.forSharedCraftMaterials());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantFreeCastForNextSpellOfChosenCardTypeEffect()),
                "{T}: The next spell you cast this turn of the chosen type can be cast without paying its mana cost."));
    }
}
