package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardOneManaOfEachColorAmongControlledEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMonocoloredPredicate;

import java.util.List;

@CardRegistration(set = "BIG", collectorNumber = "30")
public class TarnationVista extends Card {

    public TarnationVista() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardChosenColorManaEffect()),
                "{T}: Add one mana of the chosen color."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardOneManaOfEachColorAmongControlledEffect(new PermanentIsMonocoloredPredicate())),
                "{1}, {T}: For each color among monocolored permanents you control, add one mana of that color."
        ));
    }
}
