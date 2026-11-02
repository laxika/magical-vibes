package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SeekTheHeart;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "170")
public class TheArkenstone extends Card {

    public TheArkenstone() {
        setBackFaceCard(new SeekTheHeart());
        addCastingOption(new AdventureCast("{2}{W}"));
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES));
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new DrawCardEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "SeekTheHeart";
    }
}
