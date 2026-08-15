package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EMN", collectorNumber = "129")
public class GalvanicBombardment extends Card {

    public GalvanicBombardment() {
        // 2 damage plus one for every Galvanic Bombardment in your graveyard.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(
                new Sum(new Fixed(2), new CardsInGraveyard(
                        new CardNamedPredicate("Galvanic Bombardment"), CountScope.CONTROLLER))));
    }
}
