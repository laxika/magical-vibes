package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "81")
@CardRegistration(set = "FIC", collectorNumber = "170")
@CardRegistration(set = "FIC", collectorNumber = "473")
public class EmetSelchOfTheThirdSeat extends Card {

    public EmetSelchOfTheThirdSeat() {
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardTruePredicate(), 2, CostModificationScope.SELF, Zone.GRAVEYARD));

        addTrigger(Triggers.opponentLosesLife(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, true));
    }
}
