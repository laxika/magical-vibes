package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

/**
 * Target player exiles cards from the top of their library until a matching card is found. The
 * controller may cast that card without paying its mana cost. The exiled cards that were not cast
 * are then either put on the bottom of that library in a random order (Chaos Wand, Dazzling Sphinx,
 * Gr-Ma Sarumans' Footman) or, when {@code restStaysExiled} is true, left in exile (Strago and Relm).
 *
 * <p>The no-argument form is the Chaos Wand / Dazzling Sphinx effect and matches instants or
 * sorceries, bottoming the remainder. The predicate form is the Sketch and Lore shape used by Strago
 * and Relm, which also supports creature cards with optional haste and end-step sacrifice; its
 * three-argument constructor leaves the remainder in exile.
 */
public record RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect(
        CardPredicate predicate,
        boolean grantHaste,
        boolean sacrificeAtEndStep,
        boolean restStaysExiled)
        implements CombatDamageTriggerContextEffect {

    public RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect() {
        this(new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY))), false, false, false);
    }

    public RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect(CardPredicate predicate,
                                                                           boolean grantHaste,
                                                                           boolean sacrificeAtEndStep) {
        this(predicate, grantHaste, sacrificeAtEndStep, true);
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
