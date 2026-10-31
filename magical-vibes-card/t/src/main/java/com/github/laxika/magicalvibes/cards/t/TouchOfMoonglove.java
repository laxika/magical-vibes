package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDamagedCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ORI", collectorNumber = "123")
@CardRegistration(set = "PIO", collectorNumber = "372")
public class TouchOfMoonglove extends Card {

    public TouchOfMoonglove() {
        // "Target creature you control gets +1/+0 and gains deathtouch until end of turn. Whenever a
        // creature dealt damage by that creature dies this turn, its controller loses 2 life."
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new BoostTargetCreatureEffect(1, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET))
                .addSpellEffect(RegisterDelayedDamagedCreatureDeathTriggerEffect.byTargetCreature(
                        new LoseLifeEffect(2, LoseLifeRecipient.DYING_CREATURE_CONTROLLER)));
    }
}
