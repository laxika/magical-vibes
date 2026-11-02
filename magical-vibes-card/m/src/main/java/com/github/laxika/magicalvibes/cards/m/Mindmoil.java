package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutHandOnBottomOfLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "135")
public class Mindmoil extends Card {

    public Mindmoil() {
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(null, List.of(new PutHandOnBottomOfLibraryAndDrawEffect())));
    }
}
