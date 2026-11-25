package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SUM", collectorNumber = "147")
@CardRegistration(set = "3ED", collectorNumber = "147")
@CardRegistration(set = "2ED", collectorNumber = "146")
@CardRegistration(set = "LEA", collectorNumber = "145")
@CardRegistration(set = "LEB", collectorNumber = "146")
public class Earthbind extends Card {

    public Earthbind() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        TriggerOptions.DEFAULT.withInterveningIf(
                                new EnchantedPermanentMatches(
                                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                                        "enchanted creature has flying")),
                        SequenceEffect.of(
                                DealDamageEffect.toTargetCreature(2),
                                new GrantStaticEffectToSourceEffect(
                                        new RemoveKeywordEffect(
                                                Keyword.FLYING,
                                                Subjects.attachedCreature(),
                                                Duration.PERMANENT))));
    }
}
