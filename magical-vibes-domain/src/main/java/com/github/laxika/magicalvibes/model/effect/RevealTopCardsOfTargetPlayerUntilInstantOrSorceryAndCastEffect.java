package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

/**
 * Target player exiles cards from the top of their library until a matching card is found. The
 * controller may cast that card without paying its mana cost, then the remaining cards are put on
 * the bottom of that library in a random order.
 *
 * <p>The no-argument form is the original Chaos Wand / Grima effect and matches instants or
 * sorceries. The predicate form also supports cards such as Strago and Relm that include creature
 * cards, with optional haste and end-step sacrifice for creature spells cast this way.
 */
public record RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect(
        CardPredicate predicate,
        boolean grantHaste,
        boolean sacrificeAtEndStep)
        implements CombatDamageTriggerContextEffect {

    public RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect() {
        this(new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY))), false, false);
    }

    public RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect(CardPredicate predicate,
                                                                           boolean grantHaste,
                                                                           boolean sacrificeAtEndStep) {
        this.predicate = predicate;
        this.grantHaste = grantHaste;
        this.sacrificeAtEndStep = sacrificeAtEndStep;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
