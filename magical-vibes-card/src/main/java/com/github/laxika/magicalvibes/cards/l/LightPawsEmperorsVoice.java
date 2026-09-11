package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;

@CardRegistration(set = "NEO", collectorNumber = "25")
public class LightPawsEmperorsVoice extends Card {

    public LightPawsEmperorsVoice() {
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardIsAuraPredicate(),
                        new ConditionalEffect(new WasCast(),
                                new MayEffect(
                                        new SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect(true),
                                        "Search your library for an Aura card to put onto the battlefield attached to Light-Paws?"))));
    }
}
