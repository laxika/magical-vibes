package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "ECC", collectorNumber = "173")
@CardRegistration(set = "TMC", collectorNumber = "78")
public class ThrivingIsle extends Card {

    public ThrivingIsle() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseColorOnEnterEffect(CardColor.WHITE, CardColor.BLACK, CardColor.RED, CardColor.GREEN));

        // {T}: Add {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        // {T}: Add one mana of the chosen color.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardChosenColorManaEffect()),
                "{T}: Add one mana of the chosen color."
        ));
    }
}
