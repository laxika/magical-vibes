package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FlametongueKavu;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoTopCardsOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "16")
public class MineSecurity extends Card {

    public MineSecurity() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureCardIntoTopCardsOfLibraryEffect(
                        FlametongueKavu::new,
                        8,
                        List.of(new AlternateHandCast(List.of(new ManaCastingCost("{0}"))))));
    }
}
