package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.SourceLibraryPlayPermissionUsedThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandOrCastSpellFromTopOfLibraryOncePerTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "2")
@CardRegistration(set = "WHO", collectorNumber = "193")
@CardRegistration(set = "WHO", collectorNumber = "415")
@CardRegistration(set = "WHO", collectorNumber = "555")
@CardRegistration(set = "WHO", collectorNumber = "607")
@CardRegistration(set = "WHO", collectorNumber = "1006")
@CardRegistration(set = "WHO", collectorNumber = "1146")
public class TheFourthDoctor extends Card {

    public TheFourthDoctor() {
        CardIsHistoricPredicate historic = new CardIsHistoricPredicate();

        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new PlayLandOrCastSpellFromTopOfLibraryOncePerTurnEffect(historic));

        addTrigger(List.of(Triggers.youCastSpell().withFilter(historic).castFrom(Zone.LIBRARY),
                        Triggers.youPlayLand().withFilter(historic).playedFrom(Zone.LIBRARY)),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)
                        .withEventCondition(new SourceLibraryPlayPermissionUsedThisTurn()),
                CreateTokenEffect.ofFoodToken(1));
    }
}
