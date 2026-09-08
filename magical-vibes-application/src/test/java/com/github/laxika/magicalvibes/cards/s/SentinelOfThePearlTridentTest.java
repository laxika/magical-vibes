package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KembaKhaRegent;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SentinelOfThePearlTridentTest extends BaseCardTest {

    // ===== ETB with artifact (historic) =====

    @Test
    @DisplayName("Can exile an artifact you control (historic)")
    void canExileOwnArtifact() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ornithopterId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Ornithopter should be exiled
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ornithopter"));
    }

    // ===== ETB with legendary creature (historic) =====

    @Test
    @DisplayName("Can exile a legendary creature you control (historic)")
    void canExileLegendaryCreature() {
        harness.addToBattlefield(player1, new KembaKhaRegent());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID kembaId = harness.getPermanentId(player1, "Kemba, Kha Regent");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, kembaId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Kemba, Kha Regent");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kemba, Kha Regent"));
    }

    // ===== May ability =====

    @Test
    @DisplayName("Resolving triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Ornithopter"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining may ability does not exile anything")
    void decliningMaySkipsExile() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Ornithopter"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sentinel of the Pearl Trident");
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    // ===== Return at end step =====

    @Test
    @DisplayName("Exiled permanent returns at beginning of next end step")
    void exiledPermanentReturnsAtEndStep() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ornithopterId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Ornithopter is exiled
        harness.assertNotOnBattlefield(player1, "Ornithopter");

        // Advance to end step
        advanceToEndStep();

        // Ornithopter should be back on battlefield
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Returned permanent has summoning sickness")
    void returnedPermanentHasSummoningSickness() {
        harness.addToBattlefield(player1, new KembaKhaRegent());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID kembaId = harness.getPermanentId(player1, "Kemba, Kha Regent");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, kembaId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Kemba, Kha Regent");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Non-historic creature you control is not a legal target — ETB never triggers")
    void cannotTargetNonHistoricCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> creature enters; ETB finds no legal target

        // Grizzly Bears isn't historic, so the "may exile target historic permanent you control"
        // ETB has no legal target and isn't put on the stack (CR 603.3c) — no prompt appears.
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sentinel of the Pearl Trident");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent's historic permanent is not a legal target — ETB never triggers")
    void cannotTargetOpponentHistoricPermanent() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> creature enters; ETB finds no legal target

        // The Ornithopter is historic but an opponent controls it; the ETB may only target a
        // historic permanent you control, so it has no legal target and never triggers (CR 603.3c).
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    // ===== Helpers =====

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
