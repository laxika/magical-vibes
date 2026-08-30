package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SUM", collectorNumber = "147")
public class Earthbind extends Card {

    public Earthbind() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ConditionalEffect(
                                new EnchantedPermanentMatches(
                                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                                        "enchanted creature has flying"),
                                SequenceEffect.of(
                                        new DealDamageToTargetCreatureEffect(2),
                                        new GrantStaticEffectToSourceEffect(
                                                new RemoveKeywordEffect(
                                                        Keyword.FLYING,
                                                        GrantScope.ENCHANTED_CREATURE,
                                                        EffectDuration.PERMANENT)))));
    }
}
