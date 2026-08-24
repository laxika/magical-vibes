package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "170")
public class MoggWarMarshal extends Card {

    public MoggWarMarshal() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterEchoAtNextUpkeepEffect("{1}{R}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, goblinToken());
        addEffect(EffectSlot.ON_DEATH, goblinToken());
    }

    private static CreateTokenEffect goblinToken() {
        return new CreateTokenEffect("Goblin", 1, 1, CardColor.RED,
                List.of(CardSubtype.GOBLIN), Set.of(), Set.of());
    }
}
