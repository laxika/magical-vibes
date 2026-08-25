package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegeneratesIfWouldBeDestroyedEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "26")
public class KnightOfTheHolyNimbus extends Card {

    public KnightOfTheHolyNimbus() {
        addEffect(EffectSlot.STATIC, new RegeneratesIfWouldBeDestroyedEffect());

        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(PreventTargetCreatureRegenerationThisTurnEffect.forSourcePermanent()),
                "{2}: This creature can't be regenerated this turn. Only your opponents may activate this ability.")
                .withActivatableByAnyPlayer()
                .withActivatableOnlyByOpponents());
    }
}
