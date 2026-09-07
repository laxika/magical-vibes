package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "53")
public class SewerVeillanceCam extends Card {

    public SewerVeillanceCam() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new TapOrUntapTargetPermanentEffect(), "Tap or untap target creature?"));
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new MayEffect(new TapOrUntapTargetPermanentEffect(), "Tap or untap target creature?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{3}{U}, Sacrifice this artifact: Draw two cards."
        ));
    }
}
