package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "SPM", collectorNumber = "93")
@CardRegistration(set = "SPM", collectorNumber = "263")
@CardRegistration(set = "OM1", collectorNumber = "75")
public class SpiderVerse extends Card {

    public SpiderVerse() {
        addStaticEffect(new IgnoreLegendRuleEffect(
                Subjects.controlledBy(Subjects.you(), new PermanentHasSubtypePredicate(CardSubtype.SPIDER))));
        addTrigger(Triggers.youCastSpell().anySpell().notCastFrom(Zone.HAND),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new MayEffect(new CopyTriggeringSpellEffect().permanentSpellCopiesGainHaste(), "Copy that spell?"));
    }
}
