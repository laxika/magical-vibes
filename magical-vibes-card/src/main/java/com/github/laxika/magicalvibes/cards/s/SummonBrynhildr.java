package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "160")
public class SummonBrynhildr extends Card {

    public SummonBrynhildr() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ExileTopCardMayPlayThisTurnEffect(false));

        var nextCreatureGainsHaste = new RegisterDelayedControllerSpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new GrantKeywordsToCastSpellEffect(Set.of(Keyword.HASTE))),
                true,
                false);
        addEffect(EffectSlot.SAGA_CHAPTER_II, nextCreatureGainsHaste);
        addEffect(EffectSlot.SAGA_CHAPTER_III, nextCreatureGainsHaste);
    }
}
