package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "168")
public class ShepherdOfRot extends Card {

    public ShepherdOfRot() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new LoseLifeEffect(
                        new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                                CountScope.ANY_PLAYER),
                        LoseLifeRecipient.EACH_PLAYER)),
                "{T}: Each player loses 1 life for each Zombie on the battlefield."
        ));
    }
}
