package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnNextInstantOrSorceryCastFromHandToHandThisTurnEffect;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "27")
public class SoulfireGrandMaster extends Card {

    public SoulfireGrandMaster() {
        addEffect(EffectSlot.STATIC, GrantLifelinkToControllerSpellsByColorEffect.allColors());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U/R}{U/R}",
                List.of(new ReturnNextInstantOrSorceryCastFromHandToHandThisTurnEffect()),
                "{2}{U/R}{U/R}: The next time you cast an instant or sorcery spell from your hand this turn, "
                        + "put that card into your hand instead of into your graveyard as it resolves."
        ));
    }
}
