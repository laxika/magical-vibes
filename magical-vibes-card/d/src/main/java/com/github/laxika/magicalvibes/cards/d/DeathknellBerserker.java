package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "83")
public class DeathknellBerserker extends Card {

    public DeathknellBerserker() {
        // When this creature dies, if its power was 3 or greater, create a 2/2 black Zombie Berserker creature token.
        addEffect(EffectSlot.ON_DEATH, new TriggeringPermanentConditionalEffect(
                new PermanentPowerAtLeastPredicate(3),
                new CreateTokenEffect(
                        "Zombie", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.ZOMBIE, CardSubtype.BERSERKER),
                        java.util.Set.of(), java.util.Set.of())));
    }
}
