package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AvatarKuruk extends Card {

    public AvatarKuruk() {
        PermanentPredicate spirit = new PermanentHasSubtypePredicate(CardSubtype.SPIRIT);
        CreateTokenEffect spiritToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Spirit", 1, 1, null, null,
                List.of(CardSubtype.SPIRIT), Set.of(), Set.of(), false, false,
                Map.of(EffectSlot.STATIC, SequenceEffect.of(
                        new CanBeBlockedOnlyByFilterEffect(spirit, "Spirit creatures"),
                        new CanBlockOnlyIfAttackerMatchesPredicateEffect(spirit, "Spirit creatures")
                )),
                List.of(), false, false, false, 0, Set.of()
        );

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(spiritToken)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new WaterbendCost(20), new ControllerExtraTurnEffect(1)),
                "Exhaust — Waterbend {20}: Take an extra turn after this one."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
