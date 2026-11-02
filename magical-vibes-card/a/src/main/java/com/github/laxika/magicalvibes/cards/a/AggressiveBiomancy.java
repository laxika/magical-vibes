package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "69")
@CardRegistration(set = "M3C", collectorNumber = "121")
public class AggressiveBiomancy extends Card {

    public AggressiveBiomancy() {
        var fightEffect = new EnteringCreatureFightsTargetCreatureEffect();
        var copyEffect = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(),
                Set.of(),
                null,
                null,
                Map.of(),
                false,
                false,
                false,
                false,
                false,
                false,
                null,
                Set.of(),
                false,
                List.of(new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), fightEffect)),
                List.of(),
                false,
                false,
                new XValue(),
                false,
                Set.of(),
                false
        );

        target(TargetFilters.creatureYouControl()).addSpellEffect(copyEffect);
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), fightEffect);
    }
}
