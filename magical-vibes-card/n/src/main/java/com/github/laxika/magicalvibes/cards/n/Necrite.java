package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5ED", collectorNumber = "181")
@CardRegistration(set = "FEM", collectorNumber = "41a")
@CardRegistration(set = "FEM", collectorNumber = "41b")
@CardRegistration(set = "FEM", collectorNumber = "41c")
@CardRegistration(set = "FEM", collectorNumber = "121")
@CardRegistration(set = "ME2", collectorNumber = "106")
public class Necrite extends Card {

    public Necrite() {
        // Whenever this creature attacks and isn't blocked, you may sacrifice it. If you do, destroy
        // target creature defending player controls. It can't be regenerated. The target is chosen when
        // the trigger goes on the stack (CR 603.3d, with the targeting of CR 601.2c), and the sacrifice decision is made on resolution.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                        new MayEffect(
                                SequenceEffect.of(new SacrificeSelfEffect(), new DestroyTargetPermanentEffect(true)),
                                "You may sacrifice it. If you do, destroy target creature defending player controls. "
                                        + "It can't be regenerated."));
    }
}
