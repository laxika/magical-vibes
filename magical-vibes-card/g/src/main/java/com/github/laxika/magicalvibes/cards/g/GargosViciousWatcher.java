package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "172")
@CardRegistration(set = "SLD", collectorNumber = "1329")
public class GargosViciousWatcher extends Card {

    public GargosViciousWatcher() {
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.HYDRA), 4, CostModificationScope.SELF));

        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.creatureYouControlBecomesTargetOfSpell(),
                        new SourceFightsTargetCreatureEffect());
    }
}
