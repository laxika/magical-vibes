package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "258")
public class UnclaimedTerritory extends Card {

    public UnclaimedTerritory() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add one mana of any color. Spend this mana only to cast a creature spell of the chosen type.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.CHOSEN_SUBTYPE_CREATURE)),
                "{T}: Add one mana of any color. Spend this mana only to cast a creature spell of the chosen type."
        ));
    }
}
