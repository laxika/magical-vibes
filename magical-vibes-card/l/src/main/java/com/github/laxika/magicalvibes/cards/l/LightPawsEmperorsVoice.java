package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "25")
@CardRegistration(set = "SLD", collectorNumber = "1677")
@CardRegistration(set = "SLD", collectorNumber = "1877")
public class LightPawsEmperorsVoice extends Card {

    public LightPawsEmperorsVoice() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.ENCHANTMENT,
                                new PermanentHasSubtypePredicate(CardSubtype.AURA))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new WasCast(true)),
                new MayEffect(
                        new SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect(true),
                        "Search your library for an Aura card to put onto the battlefield attached to Light-Paws?"));
    }
}
