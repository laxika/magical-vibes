package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCanCastSpellsOnlyAtSorcerySpeedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "221")
public class TeferiTimeRaveler extends Card {

    public TeferiTimeRaveler() {
        addEffect(EffectSlot.STATIC, new OpponentsCanCastSpellsOnlyAtSorcerySpeedEffect());

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new GrantFlashToCardTypeUntilNextTurnEffect(CardType.SORCERY)),
                "+1: Until your next turn, you may cast sorcery spells as though they had flash."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(ReturnToHandEffect.target(), new DrawCardEffect(1)),
                "−3: Return up to one target artifact, creature, or enchantment to its owner's hand. Draw a card.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be an artifact, creature, or enchantment"
                ),
                -3,
                null,
                null,
                List.of(),
                0,
                1
        ));
    }
}
