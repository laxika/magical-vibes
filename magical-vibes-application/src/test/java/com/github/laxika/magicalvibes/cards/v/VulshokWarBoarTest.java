package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VulshokWarBoar.class, DarksteelIngot.class, DarksteelGargoyle.class, CrazedGoblin.class})
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
        harness.addToBattlefield(player2, new DarksteelIngot());

        castWarBoar();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vulshok War Boar");
        harness.assertOnBattlefield(player2, "Darksteel Ingot");
    }

    @Test
    @DisplayName("Sacrificing an artifact keeps Vulshok War Boar on the battlefield")
    void sacrificingArtifactKeepsWarBoar() {
        harness.addToBattlefield(player1, new DarksteelIngot());

        castWarBoar();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID artifactId = findPermanent(player1, "Darksteel Ingot").getId();
        harness.handlePermanentChosen(player1, artifactId);

        harness.assertOnBattlefield(player1, "Vulshok War Boar");
        harness.assertNotOnBattlefield(player1, "Darksteel Ingot");
        harness.assertInGraveyard(player1, "Darksteel Ingot");
    }

    @Test
    @DisplayName("Declining to sacrifice an artifact sacrifices Vulshok War Boar")
    void decliningSacrificesWarBoar() {
        harness.addToBattlefield(player1, new DarksteelIngot());

        castWarBoar();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Vulshok War Boar");
        harness.assertInGraveyard(player1, "Vulshok War Boar");
        harness.assertOnBattlefield(player1, "Darksteel Ingot");
    }

    @Test
    @DisplayName("Only artifacts are offered as the permanent to sacrifice")
    void onlyArtifactsAreValidChoices() {
        harness.addToBattlefield(player1, new DarksteelIngot());
        harness.addToBattlefield(player1, new DarksteelGargoyle());
        harness.addToBattlefield(player1, new CrazedGoblin());

        castWarBoar();

        harness.handleMayAbilityChosen(player1, true);

        UUID ingotId = findPermanent(player1, "Darksteel Ingot").getId();
        UUID gargoyleId = findPermanent(player1, "Darksteel Gargoyle").getId();
        UUID goblinId = findPermanent(player1, "Crazed Goblin").getId();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactlyInAnyOrder(ingotId, gargoyleId)
                .doesNotContain(goblinId);
    }

    private void castWarBoar() {
        harness.castFromHand(player1, new VulshokWarBoar(), "{2}{R}{R}");
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }
}
