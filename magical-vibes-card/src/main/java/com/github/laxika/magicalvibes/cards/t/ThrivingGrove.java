package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "430")
@CardRegistration(set = "ECC", collectorNumber = "171")
@CardRegistration(set = "TLE", collectorNumber = "261")
@CardRegistration(set = "TMC", collectorNumber = "77")
public class ThrivingGrove extends Card {

    public ThrivingGrove() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseColorOnEnterEffect(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED));

        // "Add {G} or one mana of the chosen color" is modeled as two separate mana abilities.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {G}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardChosenColorManaEffect()),
                "{T}: Add one mana of the chosen color."
        ));
    }
}
