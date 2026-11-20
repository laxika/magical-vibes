package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPermanentControllerHandRecordManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "101")
public class FriendlyFire extends Card {

    public FriendlyFire() {
        var reveal = new RevealRandomCardFromTargetPermanentControllerHandRecordManaValueEffect();
        var creatureDamage = new DealDamageToTargetCreatureEffect(new EventValue());
        var controllerDamage = new DealDamageToPlayersEffect(new EventValue(), Subjects.controllerOf(Subjects.target()));
        target(TargetFilters.creature())
                .bindEffect(reveal)
                .bindEffect(creatureDamage)
                .bindEffect(controllerDamage);
        addSpellEffect(reveal);
        addSimultaneousSpellEffects(creatureDamage, controllerDamage);
    }
}
