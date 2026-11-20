package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "140")
public class ImpossibleInferno extends Card {

    public ImpossibleInferno() {
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(6))
                .addSpellEffect(new ConditionalEffect(new Delirium(),
                        new ExileTopCardMayPlayThisTurnEffect(1, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)));
    }
}
