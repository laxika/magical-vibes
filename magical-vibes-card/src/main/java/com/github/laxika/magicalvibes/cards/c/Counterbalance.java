package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardAndCounterTriggeringSpellIfManaValueMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "31")
public class Counterbalance extends Card {

    public Counterbalance() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(null, List.of(
                        new RevealTopCardAndCounterTriggeringSpellIfManaValueMatchesEffect())),
                "Reveal the top card of your library?"));
    }
}
