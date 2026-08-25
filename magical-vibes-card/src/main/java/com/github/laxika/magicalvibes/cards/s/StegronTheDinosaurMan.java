package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "95")
public class StegronTheDinosaurMan extends Card {

    public StegronTheDinosaurMan() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new BoostTargetCreatureEffect(3, 1),
                        new GrantSubtypeUntilEndOfTurnEffect(CardSubtype.DINOSAUR, GrantScope.TARGET)
                ),
                "Dinosaur Formula — {1}{R}, Discard this card: Target creature you control gets +3/+1 and becomes a Dinosaur in addition to its other types until end of turn.",
                TargetFilters.creatureYouControl()
        ));
    }
}
