package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "[That player] adds {G}{G}": adds mana of {@code color} to another player than the resolving object's controller.
 * The recipient is {@code recipient}, or with {@code null} the stack entry's active player, the one recorded when the
 * trigger was put on the stack (a trigger on every player's turn), not the source's controller.
 *
 * <p>Not a {@link ManaProducingEffect}: a beginning-of-step or spell-cast trigger uses the stack (CR 605.1b), and a
 * targeted ability ("target player adds") isn't a mana ability (CR 605.1a). The optional restriction routes the mana
 * into a restricted bucket before the recipient can spend it.
 *
 * @param recipient who adds the mana ({@code triggeringPlayer()} for "that player" of a cast trigger,
 *                  {@code targetPlayer()} for "target player adds"); {@code null} for the recorded active player
 */
public record AwardManaToActivePlayerEffect(ManaColor color, DynamicAmount amount,
                                            ManaRestriction restriction, PlayerSubject recipient) implements CardEffect {

    public AwardManaToActivePlayerEffect(ManaColor color, DynamicAmount amount, ManaRestriction restriction) {
        this(color, amount, restriction, null);
    }

    public AwardManaToActivePlayerEffect(ManaColor color, DynamicAmount amount) {
        this(color, amount, null);
    }

    public AwardManaToActivePlayerEffect(ManaColor color, int amount) {
        this(color, new Fixed(amount), null);
    }

    public AwardManaToActivePlayerEffect(ManaColor color, int amount, ManaRestriction restriction) {
        this(color, new Fixed(amount), restriction);
    }

    /** The same mana added by {@code recipient} instead of the active player. */
    public AwardManaToActivePlayerEffect to(PlayerSubject recipient) {
        return new AwardManaToActivePlayerEffect(color, amount, restriction, recipient);
    }

    @Override
    public TargetSpec targetSpec() {
        return recipient != null && recipient.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
