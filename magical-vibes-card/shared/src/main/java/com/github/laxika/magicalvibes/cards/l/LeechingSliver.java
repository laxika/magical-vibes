package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M15", collectorNumber = "102")
@CardRegistration(set = "SLD", collectorNumber = "632")
public class LeechingSliver extends Card {

    public LeechingSliver() {
        // Whenever a Sliver you control attacks, defending player loses 1 life.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(new PermanentHasSubtypePredicate(CardSubtype.SLIVER),
                        new LoseLifeEffect(1, LoseLifeRecipient.DEFENDING_PLAYER)));
    }
}
