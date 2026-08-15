package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AviaryMechanicTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may prompt to return another permanent you control")
    void etbPromptsOptionalReturn() {
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");

        castAndResolveSpell();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        UUID mechanicId = harness.getPermanentId(player1, "Aviary Mechanic");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(islandId);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(mechanicId);
    }

    @Test
    @DisplayName("Accepting the may ability returns the chosen permanent to its owner's hand")
    void acceptingReturnsChosenPermanent() {
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");

        castAndResolveSpell();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, islandId);

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Aviary Mechanic");
    }

    @Test
    @DisplayName("Declining the may ability leaves permanents on the battlefield")
    void decliningLeavesPermanentsOnBattlefield() {
        harness.addToBattlefield(player1, new Island());

        castAndResolveSpell();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Aviary Mechanic");
    }

    @Test
    @DisplayName("Opponent permanents are not valid choices")
    void opponentPermanentsAreNotChoices() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID islandId = harness.getPermanentId(player1, "Island");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolveSpell();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(islandId)
                .doesNotContain(bearsId);
    }

    private void castAndResolveSpell() {
        harness.setHand(player1, List.of(new AviaryMechanic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
