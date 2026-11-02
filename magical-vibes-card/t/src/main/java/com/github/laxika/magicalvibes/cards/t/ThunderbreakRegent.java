package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringSpellControllerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "162")
@CardRegistration(set = "TDC", collectorNumber = "241")
@CardRegistration(set = "AFC", collectorNumber = "147")
@CardRegistration(set = "SCD", collectorNumber = "163")
@CardRegistration(set = "FDC", collectorNumber = "181")
public class ThunderbreakRegent extends Card {

    public ThunderbreakRegent() {
        addTrigger(Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.DRAGON),
                        new DealDamageToTriggeringSpellControllerEffect(3)));
    }
}
