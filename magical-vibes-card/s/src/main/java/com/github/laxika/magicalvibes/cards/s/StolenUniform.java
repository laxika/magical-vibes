package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterControlLossUnattachTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "75")
@CardRegistration(set = "FIN", collectorNumber = "332")
public class StolenUniform extends Card {

    public StolenUniform() {
        target(TargetFilters.creatureYouControl());
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment"))
                .addSpellEffect(new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN))
                .addSpellEffect(RegisterControlLossUnattachTriggerEffect.thisTurnIfAttachedToCreatureYouControl());
        addSpellEffect(AttachTargetEquipmentToTargetCreatureEffect.creatureFirst());
    }
}
