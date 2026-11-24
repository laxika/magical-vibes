package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.effect.ExilePlayPermissionEffect;

import java.util.Objects;

/**
 * "Until end of turn, you may play cards [you own] exiled with this": a source permanent's this-turn play permission
 * over the cards exiled with it, kept as a floating effect on the source so it also covers the cards exiled with it
 * later that turn (CR 611.2c: an effect that changes only the rules of the game isn't limited to the objects there as
 * it began). Each card the exile service exiles with the source that {@link #cards()} names gets {@link #permission()},
 * without its cost change if it is a land card ("spells you cast this way cost less").
 *
 * @param cards      which cards exiled with the source it covers: {@code Subjects.exiledWithSource(...)}
 * @param permission the play permission each covered card gets, with its holder
 */
public record ExilePlayGrantBySource(CardSubject cards, ExilePlayPermissionEffect permission) implements CardEffect {

    public ExilePlayGrantBySource {
        Objects.requireNonNull(cards, "cards");
        Objects.requireNonNull(permission, "permission");
        if (!(cards instanceof CardSubject.ExiledWithSource)) {
            throw new IllegalArgumentException("The grant covers cards exiled with its source: " + cards);
        }
    }
}
