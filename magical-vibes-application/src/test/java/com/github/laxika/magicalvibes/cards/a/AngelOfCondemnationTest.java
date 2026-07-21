package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AngelOfCondemnationTest extends BaseCardTest {

    // ===== Ability 0: {2}{W}, {T}: blink another target creature =====

    @Test
    @DisplayName("Blink ability exiles the target creature")
    void blinkExilesTargetCreature() {
        addCreatureReady(player1, new AngelOfCondemnation());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Blinked creature returns under its owner's control at the next end step")
    void blinkedCreatureReturnsAtNextEndStep() {
        addCreatureReady(player1, new AngelOfCondemnation());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Ability 1: {2}{W}, {T}, Exert: exile another target creature until this leaves =====

    @Test
    @DisplayName("Exert ability exiles the target and keeps the Angel tapped through its next untap")
    void exertAbilityExilesAndExerts() {
        Permanent angel = addCreatureReady(player1, new AngelOfCondemnation());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();

        assertThat(angel.isTapped()).isTrue();
        assertThat(angel.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Exiled creature returns when the Angel leaves the battlefield")
    void exiledCreatureReturnsWhenAngelLeaves() {
        addCreatureReady(player1, new AngelOfCondemnation());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Bounce the Angel so it leaves the battlefield.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID angelId = harness.getPermanentId(player1, "Angel of Condemnation");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, angelId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Blink ability cannot target the Angel itself")
    void blinkCannotTargetSelf() {
        addCreatureReady(player1, new AngelOfCondemnation());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID angelId = harness.getPermanentId(player1, "Angel of Condemnation");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, angelId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exert ability cannot target the Angel itself")
    void exertCannotTargetSelf() {
        addCreatureReady(player1, new AngelOfCondemnation());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID angelId = harness.getPermanentId(player1, "Angel of Condemnation");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, angelId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
