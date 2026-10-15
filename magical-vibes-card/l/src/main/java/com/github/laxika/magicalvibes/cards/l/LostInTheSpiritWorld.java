package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "224")
public class LostInTheSpiritWorld extends Card {

    public LostInTheSpiritWorld() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        addEffect(EffectSlot.SPELL, spiritToken());
    }

    private static CreateTokenEffect spiritToken() {
        PermanentPredicate spirit = new PermanentHasSubtypePredicate(CardSubtype.SPIRIT);
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Spirit", 1, 1, null, null,
                List.of(CardSubtype.SPIRIT), Set.of(), Set.of(), false, false,
                Map.of(EffectSlot.STATIC, SequenceEffect.of(
                        new CanBeBlockedOnlyByFilterEffect(spirit, "Spirit creatures"),
                        new CanBlockOnlyIfAttackerMatchesPredicateEffect(spirit, "Spirit creatures")
                )),
                List.of(), false, false, false, 0, Set.of()
        );
    }
}
