package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "19")
public class MasteryOfTheUnseen extends Card {

    public MasteryOfTheUnseen() {
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP,
                new GainLifeEffect(new PermanentCount(
                        new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));
        addActivatedAbility(new ActivatedAbility(false, "{3}{W}",
                List.of(new ManifestTopCardEffect()),
                "{3}{W}: Manifest the top card of your library."));
    }
}
