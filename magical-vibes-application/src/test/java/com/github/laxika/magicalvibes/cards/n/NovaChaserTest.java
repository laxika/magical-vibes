package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FlamekinBladewhirl;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NovaChaser.class, FlamekinBladewhirl.class, GrizzlyBears.class, Unsummon.class})
class NovaChaserTest extends BaseCardTest {

    private void castNovaChaser() {
        harness.castFromHand(player1, new NovaChaser(), "{3}{R}");
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no other Elemental")
    void autoSacrificesWithNoOtherElemental() {
        castNovaChaser();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice

        harness.assertNotOnBattlefield(player1, "Nova Chaser");
        harness.assertInGraveyard(player1, "Nova Chaser");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A non-Elemental creature does not satisfy the champion cost")
    void nonElementalDoesNotSatisfyChampion() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castNovaChaser();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice (no valid Elemental)

        harness.assertNotOnBattlefield(player1, "Nova Chaser");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Nova Chaser");
    }

    @Test
    @DisplayName("ETB with another Elemental prompts champion choice")
    void etbWithElementalPromptsChoice() {
        harness.addToBattlefield(player1, new FlamekinBladewhirl());
        castNovaChaser();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.assertOnBattlefield(player1, "Nova Chaser");
    }

    @Test
    @DisplayName("Championing an Elemental exiles it and keeps Nova Chaser")
    void championingExilesElemental() {
        harness.addToBattlefield(player1, new FlamekinBladewhirl());
        castNovaChaser();
        harness.passBothPriorities();

        UUID elementalId = harness.getPermanentId(player1, "Flamekin Bladewhirl");
        harness.handlePermanentChosen(player1, elementalId);

        harness.assertOnBattlefield(player1, "Nova Chaser");
        harness.assertNotOnBattlefield(player1, "Flamekin Bladewhirl");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Flamekin Bladewhirl"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Championed Elemental returns when Nova Chaser leaves the battlefield")
    void championedElementalReturnsWhenNovaChaserLeaves() {
        harness.addToBattlefield(player1, new FlamekinBladewhirl());
        castNovaChaser();
        harness.passBothPriorities();

        UUID elementalId = harness.getPermanentId(player1, "Flamekin Bladewhirl");
        harness.handlePermanentChosen(player1, elementalId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID novaId = harness.getPermanentId(player1, "Nova Chaser");
        harness.castAndResolveInstant(player1, 0, novaId);

        harness.assertNotOnBattlefield(player1, "Nova Chaser");
        harness.assertNotOnBattlefield(player1, "Flamekin Bladewhirl");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // resolve the separate leaves-the-battlefield trigger
        harness.assertOnBattlefield(player1, "Flamekin Bladewhirl");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Flamekin Bladewhirl"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Controller may sacrifice Nova Chaser instead of exiling an available Elemental")
    void mayDeclineChampionWithAvailableElemental() {
        harness.addToBattlefield(player1, new FlamekinBladewhirl());
        castNovaChaser();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Nova Chaser");
        harness.assertNotOnBattlefield(player1, "Nova Chaser");
        harness.assertOnBattlefield(player1, "Flamekin Bladewhirl");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Elemental cannot be championed")
    void opponentsElementalDoesNotSatisfyChampion() {
        harness.addToBattlefield(player2, new FlamekinBladewhirl());
        castNovaChaser();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nova Chaser");
        harness.assertOnBattlefield(player2, "Flamekin Bladewhirl");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Elemental exiled after Nova Chaser has left remains exiled")
    void championAfterSourceLeavesDoesNotReturnElemental() {
        harness.addToBattlefield(player1, new FlamekinBladewhirl());
        castNovaChaser();
        UUID novaId = harness.getPermanentId(player1, "Nova Chaser");
        UUID elementalId = harness.getPermanentId(player1, "Flamekin Bladewhirl");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, novaId);
        harness.assertInHand(player1, "Nova Chaser");

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, elementalId);

        harness.assertNotOnBattlefield(player1, "Flamekin Bladewhirl");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Flamekin Bladewhirl"));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }
}
