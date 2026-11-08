package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellXValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "68")
@CardRegistration(set = "WHO", collectorNumber = "673")
public class Exterminate extends Card {

    public Exterminate() {
        addCastingCost(new TapAnyNumberOfPermanentsCost(new PermanentHasSubtypePredicate(CardSubtype.DALEK)));
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentThenEffect(new LoseLifeEffect(3), ThenEffectRecipient.TARGET_CONTROLLER));
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new SpellXAtLeast(1)),
                new CopyTriggeringSpellEffect().withCopies(new TriggeringSpellXValue()));
    }
}
