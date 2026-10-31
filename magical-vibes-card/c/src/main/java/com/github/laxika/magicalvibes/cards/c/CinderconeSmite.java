package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerWasNotStartingPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YBLB", collectorNumber = "15")
public class CinderconeSmite extends Card {

    public CinderconeSmite() {
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(2))
                .addSpellEffect(new ConditionalEffect(
                        new ControllerWasNotStartingPlayer(), CreateTokenEffect.ofTreasureToken(1)));
    }
}
