package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerToughnessFromEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;

@CardRegistration(set = "OGW", collectorNumber = "2")
public class EldraziMimic extends Card {

    public EldraziMimic() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardIsColorlessPredicate(),
                        new MayEffect(
                                new SetSelfBasePowerToughnessFromEnteringCreatureEffect(),
                                "Have Eldrazi Mimic's base power and toughness become equal to that creature's power and toughness?")));
    }
}
