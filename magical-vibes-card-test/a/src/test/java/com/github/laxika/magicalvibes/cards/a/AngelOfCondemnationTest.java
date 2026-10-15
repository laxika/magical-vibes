package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelOfCondemnation.class, FrilledSandwalla.class, Unsummon.class})
class AngelOfCondemnationTest extends BaseCardTest {

    @Test
    @DisplayName("Blink ability exiles the target creature")
    void blinkExilesTargetCreature() {
        addCreatureReady(player1, new AngelOfCondemnation());
        addCreatureReady(player2, new FrilledSandwalla());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Frilled Sandwalla");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Frilled Sandwalla");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Frilled Sandwalla"));
    }

    @Test
    @DisplayName("Blinked creature returns under its owner's control at the next end step")
    void blinkedCreatureReturnsAtNextEndStep() {
        addCreatureReady(player1, new AngelOfCondemnation());
        addCreatureReady(player2, new FrilledSandwalla());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Frilled Sandwalla");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Frilled Sandwalla");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Frilled Sandwalla");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Frilled Sandwalla"));
    }

    @Test
    @DisplayName("Exert ability exiles the target and keeps the Angel tapped through its next untap")
    void exertAbilityExilesAndExerts() {
        Permanent angel = addCreatureReady(player1, new AngelOfCondemnation());
        addCreatureReady(player2, new FrilledSandwalla());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Frilled Sandwalla");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Frilled Sandwalla");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Frilled Sandwalla"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();

        assertThat(angel.isTapped()).isTrue();
        assertThat(angel.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Exiled creature returns when the Angel leaves the battlefield")
    void exiledCreatureReturnsWhenAngelLeaves() {
        addCreatureReady(player1, new AngelOfCondemnation());
        addCreatureReady(player2, new FrilledSandwalla());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Frilled Sandwalla");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Frilled Sandwalla"));

        // Bounce the Angel so it leaves the battlefield.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID angelId = harness.getPermanentId(player1, "Angel of Condemnation");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, angelId);

        harness.assertOnBattlefield(player2, "Frilled Sandwalla");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Frilled Sandwalla"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

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

    @Test
    @DisplayName("Exert is paid immediately when activating the ability")
    void exertIsPaidBeforeResolution() {
        Permanent angel = addCreatureReady(player1, new AngelOfCondemnation());
        Permanent target = addCreatureReady(player2, new FrilledSandwalla());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(angel.isTapped()).isTrue();
        assertThat(angel.getSkipUntapCount()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Frilled Sandwalla");
    }

    @Test
    @DisplayName("Exert remains paid when the target leaves before resolution")
    void exertPersistsWhenTargetBecomesIllegal() {
        Permanent angel = addCreatureReady(player1, new AngelOfCondemnation());
        Permanent target = addCreatureReady(player2, new FrilledSandwalla());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Frilled Sandwalla");
        harness.performUntapStep(player1);
        assertThat(angel.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(angel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Successfully exerted Angel skips only its next untap")
    void exertSkipsOnlyNextUntap() {
        Permanent angel = addCreatureReady(player1, new AngelOfCondemnation());
        Permanent target = addCreatureReady(player2, new FrilledSandwalla());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(angel.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(angel.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(angel.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player2, "Frilled Sandwalla");
    }

    @Test
    @DisplayName("Exert ability does not exile if Angel leaves before resolution")
    void exertDoesNotExileAfterSourceLeaves() {
        Permanent angel = addCreatureReady(player1, new AngelOfCondemnation());
        Permanent target = addCreatureReady(player2, new FrilledSandwalla());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, angel.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Angel of Condemnation");
        harness.assertOnBattlefield(player2, "Frilled Sandwalla");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Blink still exiles and returns the creature if Angel leaves before resolution")
    void blinkWorksAfterSourceLeaves() {
        Permanent angel = addCreatureReady(player1, new AngelOfCondemnation());
        Permanent target = addCreatureReady(player2, new FrilledSandwalla());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, angel.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Frilled Sandwalla");
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Frilled Sandwalla");
        harness.assertInHand(player1, "Angel of Condemnation");
    }

    @Test
    @DisplayName("Blink return uses a delayed trigger that can be responded to")
    void blinkReturnWaitsForDelayedTriggerResolution() {
        addCreatureReady(player1, new AngelOfCondemnation());
        Permanent target = addCreatureReady(player2, new FrilledSandwalla());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player2, "Frilled Sandwalla");
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Frilled Sandwalla");
    }
}
