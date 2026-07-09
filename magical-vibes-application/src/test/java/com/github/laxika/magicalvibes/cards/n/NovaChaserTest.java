package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FlamekinBladewhirl;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NovaChaserTest extends BaseCardTest {

    private void castNovaChaser() {
        harness.setHand(player1, List.of(new NovaChaser()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no other Elemental")
    void autoSacrificesWithNoOtherElemental() {
        castNovaChaser();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nova Chaser"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nova Chaser"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A non-Elemental creature does not satisfy the champion cost")
    void nonElementalDoesNotSatisfyChampion() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castNovaChaser();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice (no valid Elemental)

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nova Chaser"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nova Chaser"));
    }

    @Test
    @DisplayName("ETB with another Elemental prompts champion choice")
    void etbWithElementalPromptsChoice() {
        harness.addToBattlefield(player1, new FlamekinBladewhirl());
        castNovaChaser();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nova Chaser"));
    }

    @Test
    @DisplayName("Championing an Elemental exiles it and keeps Nova Chaser")
    void championingExilesElemental() {
        harness.addToBattlefield(player1, new FlamekinBladewhirl());
        castNovaChaser();
        harness.passBothPriorities();

        UUID elementalId = harness.getPermanentId(player1, "Flamekin Bladewhirl");
        harness.handlePermanentChosen(player1, elementalId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nova Chaser"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Flamekin Bladewhirl"));
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
        harness.castInstant(player1, 0, novaId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nova Chaser"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Flamekin Bladewhirl"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Flamekin Bladewhirl"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }
}
