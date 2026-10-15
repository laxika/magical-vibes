package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "3")
public class Alms extends Card {

    public Alms() {
        // {1}, Exile the top card of your graveyard: Prevent the next 1 damage that would be dealt
        // to target creature this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ExileTopCardOfGraveyardCost(), PreventDamageEffect.nextToTargetCreature(1)),
                "{1}, Exile the top card of your graveyard: Prevent the next 1 damage that would be "
                        + "dealt to target creature this turn.",
                TargetFilters.creature()
        ));
    }
}
