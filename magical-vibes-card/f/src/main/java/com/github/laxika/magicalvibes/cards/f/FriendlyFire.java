package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "101")
public class FriendlyFire extends Card {

    public FriendlyFire() {
        var reveal = RevealCardsFromHandEffect.atRandom(Subjects.controllerOf(Subjects.target()), 1);
        var creatureDamage = DealDamageEffect.toTargetCreature(AmountThisWay.revealedManaValue());
        var controllerDamage = new DealDamageToPlayersEffect(AmountThisWay.revealedManaValue(), Subjects.controllerOf(Subjects.target()));
        target(TargetFilters.creature())
                .bindEffect(reveal)
                .bindEffect(creatureDamage)
                .bindEffect(controllerDamage);
        addSpellEffect(reveal);
        addSimultaneousSpellEffects(creatureDamage, controllerDamage);
    }
}
