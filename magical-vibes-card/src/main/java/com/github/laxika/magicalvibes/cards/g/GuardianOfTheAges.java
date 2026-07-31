package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SelfHasKeyword;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfKeywordIndefinitelyEffect;

@CardRegistration(set = "M14", collectorNumber = "211")
public class GuardianOfTheAges extends Card {

    public GuardianOfTheAges() {
        // Defender is auto-loaded from Scryfall.
        // When a creature attacks you or a planeswalker you control, if this creature has
        // defender, it loses defender and gains trample (both indefinitely).
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new ConditionalEffect(new SelfHasKeyword(Keyword.DEFENDER),
                        SequenceEffect.of(
                                new RemoveKeywordEffect(Keyword.DEFENDER, GrantScope.SELF, EffectDuration.PERMANENT),
                                new SetSelfKeywordIndefinitelyEffect(Keyword.TRAMPLE, true))));
    }
}
