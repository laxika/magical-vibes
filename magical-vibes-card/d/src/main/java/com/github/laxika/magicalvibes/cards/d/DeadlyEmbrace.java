package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "557")
public class DeadlyEmbrace extends Card {

    public DeadlyEmbrace() {
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new DrawCardEffect(
                        new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));
    }
}
