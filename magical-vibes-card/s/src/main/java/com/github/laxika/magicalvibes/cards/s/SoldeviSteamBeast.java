package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "133a")
@CardRegistration(set = "ALL", collectorNumber = "133b")
public class SoldeviSteamBeast extends Card {

    public SoldeviSteamBeast() {
        // Whenever this creature becomes tapped, target opponent gains 2 life.
        // (Two-player: the single opponent is derived, so no target is chosen.)
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(),
                new GainLifeEffect(new Fixed(2), GainLifeRecipient.OPPONENT)));

        // {2}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new RegenerateEffect()),
                "{2}: Regenerate Soldevi Steam Beast."
        ));
    }
}
