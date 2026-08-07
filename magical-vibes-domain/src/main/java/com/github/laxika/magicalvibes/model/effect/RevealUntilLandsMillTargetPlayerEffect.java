package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * A player reveals cards from the top of their library until {@code landCount} land cards are
 * revealed (or the library empties). That player puts all cards revealed this way into their
 * graveyard.
 * <p>
 * {@code recipient} selects who reveals. {@link MillRecipient#TARGET_PLAYER} (the compact-constructor
 * default) uses the stack entry's player target and contributes that player target itself — Mind
 * Funeral ({@code landCount = 4}, the card restricts the choice to an opponent).
 * {@link MillRecipient#TARGET_PERMANENT_CONTROLLER} instead reveals for the controller of the
 * targeted <em>permanent</em> and adds no target of its own, so pair it with a sibling effect that
 * owns the permanent target and list this effect first, while the permanent is still on the
 * battlefield — Destroy the Evidence ({@code landCount = 1}).
 * {@link MillRecipient#EACH_OPPONENT} reveals for every player other than the effect's controller
 * and adds no target of its own — Consuming Aberration ({@code landCount = 1}), Mind Grind
 * ({@code landCount = new XValue()}).
 * <p>
 * {@code landCount} is a {@link DynamicAmount} so the land count can come from the spell's paid X.
 */
public record RevealUntilLandsMillTargetPlayerEffect(DynamicAmount landCount, MillRecipient recipient) implements CardEffect {

    public RevealUntilLandsMillTargetPlayerEffect(int landCount) {
        this(new Fixed(landCount), MillRecipient.TARGET_PLAYER);
    }

    public RevealUntilLandsMillTargetPlayerEffect(int landCount, MillRecipient recipient) {
        this(new Fixed(landCount), recipient);
    }

    public RevealUntilLandsMillTargetPlayerEffect(DynamicAmount landCount) {
        this(landCount, MillRecipient.TARGET_PLAYER);
    }

    @Override
    public TargetSpec targetSpec() {
        return recipient == MillRecipient.TARGET_PLAYER
                ? TargetSpec.harmful(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
