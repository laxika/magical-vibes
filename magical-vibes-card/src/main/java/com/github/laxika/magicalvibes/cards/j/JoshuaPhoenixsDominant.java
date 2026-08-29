package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PhoenixWardenOfFire;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "229")
@CardRegistration(set = "FIN", collectorNumber = "397")
@CardRegistration(set = "FIN", collectorNumber = "494")
@CardRegistration(set = "FIN", collectorNumber = "542")
public class JoshuaPhoenixsDominant extends Card {

    public JoshuaPhoenixsDominant() {
        setBackFaceCard(new PhoenixWardenOfFire());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscardUpToThenDrawThatManyEffect(2));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{R}{W}",
                List.of(new ExileSelfAndReturnTransformedEffect()),
                "{3}{R}{W}, {T}: Exile Joshua, then return it to the battlefield transformed under its owner's control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "PhoenixWardenOfFire";
    }
}
