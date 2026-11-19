package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * A player looks at the top {@code count} cards of a library, puts any number on the bottom in any order, and puts
 * the rest on top in any order. Amount may be fixed ("scry 2") or dynamic ("scry X, where X is the number of Zombies
 * you control"). {@code player} names whose library it is (the first player it names; the controller's own when it
 * names nobody); that player makes the choices ("its controller scries 1", "an opponent scries 2"), except as below.
 *
 * <p>{@code decisionMaker} only matters for a {@code targetPlayer()} library: the target makes the choices by default
 * ("target player scries", Bumi, King of Three Trials), while {@link LibraryDecisionMaker#CONTROLLER} leaves the
 * arrangement to the controller of the spell or ability (Ransack looks at a target player's library but the caster
 * arranges it).
 *
 * @param player whose library; only {@code targetPlayer()} declares a (benign) player target
 */
public record ScryEffect(DynamicAmount count, PlayerSubject player, boolean causesScryTriggers,
                         LibraryDecisionMaker decisionMaker) implements CardEffect {

    public ScryEffect(DynamicAmount count, PlayerSubject player, boolean causesScryTriggers) {
        this(count, player, causesScryTriggers, defaultDecisionMaker(player));
    }

    public ScryEffect(DynamicAmount count, PlayerSubject player) {
        this(count, player, true);
    }

    /** Shares the top/bottom choice for operations such as clash that are not scry events. */
    public ScryEffect(int count, PlayerSubject player, boolean causesScryTriggers) {
        this(new Fixed(count), player, causesScryTriggers);
    }

    public ScryEffect(DynamicAmount count) {
        this(count, Subjects.you());
    }

    public ScryEffect(int count) {
        this(new Fixed(count), Subjects.you());
    }

    public ScryEffect(int count, PlayerSubject player) {
        this(new Fixed(count), player);
    }

    private static LibraryDecisionMaker defaultDecisionMaker(PlayerSubject player) {
        return player.declaresTarget()
                ? LibraryDecisionMaker.TARGET_PLAYER
                : LibraryDecisionMaker.CONTROLLER;
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
