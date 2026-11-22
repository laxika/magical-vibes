package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * "Exile a [filter] card from your hand", "[that player] exiles a card from their hand": the player chooses one of
 * their matching cards as the effect resolves; with none, nothing is exiled. The step pauses at its end on the choice
 * and records its {@code StepKind.EXILE} as it is answered, so "if you do", "a card exiled this way" and "it" are
 * later steps ({@code cardsThisWay(EXILE)}).
 *
 * @param player   who chooses from their own hand
 * @param filter   which cards may be chosen; {@code null} for any
 * @param faceDown "exile it face down"
 */
public record ExileCardFromHandEffect(PlayerSubject player, CardPredicate filter, boolean faceDown)
        implements CardEffect {

    public ExileCardFromHandEffect {
        Objects.requireNonNull(player, "player");
    }

    /** "Exile a card from your hand." */
    public ExileCardFromHandEffect() {
        this(Subjects.you(), null, false);
    }

    /** "Exile a [filter] card from your hand." */
    public ExileCardFromHandEffect(CardPredicate filter) {
        this(Subjects.you(), filter, false);
    }

    /** This exile "face down". */
    public ExileCardFromHandEffect exiledFaceDown() {
        return new ExileCardFromHandEffect(player, filter, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
