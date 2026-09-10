package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeDuration;

@CardRegistration(set = "OHOP", collectorNumber = "39")
public class UndercityReaches extends Card {

    public UndercityReaches() {
        MayEffect draw = new MayEffect(
                new DrawCardForTargetPlayerEffect(1),
                "Draw a card?",
                null,
                MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER);
        addEffect(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU, draw);
        addEffect(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT, draw);
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration.REST_OF_GAME));
    }
}
