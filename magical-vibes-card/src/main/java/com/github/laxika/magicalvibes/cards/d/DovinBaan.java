package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticOrbEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "179")
public class DovinBaan extends Card {

    public DovinBaan() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new BoostTargetCreatureEffect(-3, 0, GrantDuration.UNTIL_YOUR_NEXT_TURN),
                        new LockTargetPermanentEffect(false, false, true, EffectDuration.UNTIL_YOUR_NEXT_TURN)),
                "+1: Until your next turn, up to one target creature gets -3/-0 and its activated abilities can't be activated.",
                null,
                +1,
                null,
                null,
                List.of(TargetFilters.creature()),
                0,
                1));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new GainLifeEffect(2), new DrawCardEffect(1)),
                "-1: You gain 2 life and draw a card."));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new StaticOrbEffect(2, null, false, true)),
                        "Your opponents can't untap more than two permanents during their untap steps.")),
                "-7: You get an emblem with \"Your opponents can't untap more than two permanents during their untap steps.\""));
    }
}
