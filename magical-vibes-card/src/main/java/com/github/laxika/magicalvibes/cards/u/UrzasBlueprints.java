package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "137")
public class UrzasBlueprints extends Card {

    public UrzasBlueprints() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterEchoAtNextUpkeepEffect("{6}"));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect()),
                "{T}: Draw a card."
        ));
    }
}
