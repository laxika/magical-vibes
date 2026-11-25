package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "178")
@CardRegistration(set = "DDK", collectorNumber = "58")
public class BlazingSalvo extends Card {

    public BlazingSalvo() {
        target(TargetFilters.creature()).addSpellEffect(new MayEffect(
                new DealDamageToPlayersEffect(5, Subjects.controllerOf(Subjects.target())),
                "Have Blazing Salvo deal 5 damage to you?",
                DealDamageEffect.toTargetCreature(3),
                MayChoicePlayer.TARGET_PERMANENT_CONTROLLER));
    }
}
