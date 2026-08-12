package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VulshokWarBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-sacrifices when its controller has no artifact")
    void autoSacrificesWithoutArtifact() {
        castWarBoar();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Vulshok War Boar");
        harness.assertInGraveyard(player1, "Vulshok War Boar");
    }

    @Test
    @DisplayName("An opponent's artifact does not satisfy the requirement")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player2, new Ornithopter());

        castWarBoar();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vulshok War Boar");
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Sacrificing an artifact keeps Vulshok War Boar on the battlefield")
    void sacrificingArtifactKeepsWarBoar() {
        harness.addToBattlefield(player1, new Ornithopter());

        castWarBoar();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID artifactId = findPermanent(player1, "Ornithopter").getId();
        harness.handlePermanentChosen(player1, artifactId);

        harness.assertOnBattlefield(player1, "Vulshok War Boar");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Declining to sacrifice an artifact sacrifices Vulshok War Boar")
    void decliningSacrificesWarBoar() {
        harness.addToBattlefield(player1, new DarksteelRelic());

        castWarBoar();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Vulshok War Boar");
        harness.assertInGraveyard(player1, "Vulshok War Boar");
        harness.assertOnBattlefield(player1, "Darksteel Relic");
    }

    @Test
    @DisplayName("Only artifacts are offered as the permanent to sacrifice")
    void onlyArtifactsAreValidChoices() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new DarksteelRelic());

        castWarBoar();

        harness.handleMayAbilityChosen(player1, true);

        UUID artifactId = findPermanent(player1, "Ornithopter").getId();
        UUID relicId = findPermanent(player1, "Darksteel Relic").getId();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(artifactId, relicId);
    }

    private void castWarBoar() {
        harness.setHand(player1, List.of(new VulshokWarBoar()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }
}
