package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaxingMoon.class, WolfbittenCaptive.class, GrizzlyBears.class})
class WaxingMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Waxing Moon transforms a targeted Werewolf and grants trample to your creatures")
    void transformsWerewolfAndGrantsTrample() {
        Permanent werewolf = addCreatureReady(player1, new WolfbittenCaptive());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, werewolf.getId());
        harness.passBothPriorities();

        assertThat(werewolf.isTransformed()).isTrue();
        assertThat(werewolf.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(otherCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponentCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Waxing Moon may be cast without transforming a Werewolf")
    void mayChooseNoTarget() {
        Permanent werewolf = addCreatureReady(player1, new WolfbittenCaptive());
        harness.setHand(player1, List.of(new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(werewolf.isTransformed()).isFalse();
        assertThat(werewolf.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Waxing Moon rejects a target that is not a Werewolf you control")
    void rejectsIllegalTarget() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Werewolf you control");
    }

    @Test
    @DisplayName("Waxing Moon's trample effect wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Waxing Moon can transform a Werewolf back to its front face")
    void transformsBackToFrontFace() {
        Permanent werewolf = addCreatureReady(player1, new WolfbittenCaptive());
        harness.setHand(player1, List.of(new WaxingMoon(), new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, werewolf.getId());
        harness.passBothPriorities();
        assertThat(werewolf.isTransformed()).isTrue();

        harness.castInstant(player1, 0, werewolf.getId());
        harness.passBothPriorities();

        assertThat(werewolf.isTransformed()).isFalse();
        assertThat(werewolf.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Waxing Moon rejects a non-Werewolf controlled by its caster")
    void rejectsOwnNonWerewolf() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Werewolf you control");
    }

    @Test
    @DisplayName("Waxing Moon rejects an opponent's Werewolf")
    void rejectsOpponentsWerewolf() {
        Permanent werewolf = addCreatureReady(player2, new WolfbittenCaptive());
        harness.setHand(player1, List.of(new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, werewolf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Werewolf you control");
    }

    @Test
    @DisplayName("Waxing Moon grants no trample if its chosen target leaves the battlefield")
    void doesNotResolveWhenTargetLeavesBattlefield() {
        Permanent werewolf = addCreatureReady(player1, new WolfbittenCaptive());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, werewolf.getId());
        gd.playerBattlefields.get(player1.getId()).remove(werewolf);
        gd.playerGraveyards.get(player1.getId()).add(werewolf.getCard());
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof WaxingMoon);
    }

    @Test
    @DisplayName("Waxing Moon affects creatures present at resolution but not later entrants")
    void grantsTrampleOnlyToCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        Permanent beforeResolution = addCreatureReady(player1, new GrizzlyBears());
        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new GrizzlyBears());

        assertThat(beforeResolution.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(afterResolution.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
