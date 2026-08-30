package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegeneratesIfWouldBeDestroyedEffect;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "6")
public class ClergyOfTheHolyNimbus extends Card {

    public ClergyOfTheHolyNimbus() {
        addEffect(EffectSlot.STATIC, new RegeneratesIfWouldBeDestroyedEffect());

        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(PreventTargetCreatureRegenerationThisTurnEffect.forSourcePermanent()),
                "{1}: This creature can't be regenerated this turn. Only your opponents may activate this ability.")
                .withActivatableByAnyPlayer()
                .withActivatableOnlyByOpponents());
    }
}
