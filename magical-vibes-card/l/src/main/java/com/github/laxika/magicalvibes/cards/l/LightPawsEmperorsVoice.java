package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "25")
@CardRegistration(set = "SLD", collectorNumber = "1677")
@CardRegistration(set = "SLD", collectorNumber = "1877")
public class LightPawsEmperorsVoice extends Card {

    public LightPawsEmperorsVoice() {
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardIsAuraPredicate(),
                        new ConditionalEffect(new WasCast(true),
                                new MayEffect(
                                        new SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect(true),
                                        "Search your library for an Aura card to put onto the battlefield attached to Light-Paws?"))));
    }
}
