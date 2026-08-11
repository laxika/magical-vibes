package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsDistinctPermanentNamesCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "M20", collectorNumber = "247")
public class FieldOfTheDead extends Card {

    public FieldOfTheDead() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        ConditionalEffect landfall = new ConditionalEffect(
                new ControlsDistinctPermanentNamesCount(7, new PermanentIsLandPredicate()),
                CreateTokenEffect.blackZombie(1));

        // Whenever this land enters, if you control seven or more lands with different names, create
        // a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, landfall);

        // Whenever another land you control enters, use the same condition and token effect.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, landfall);
    }
}
