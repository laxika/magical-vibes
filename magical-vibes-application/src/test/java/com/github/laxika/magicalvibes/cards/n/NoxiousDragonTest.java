package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NoxiousDragonTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, may destroy a creature with mana value 3 or less")
    void diesMayDestroySmallCreature() {
        harness.addToBattlefield(player1, new NoxiousDragon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID dragonId = harness.getPermanentId(player1, "Noxious Dragon");
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        killDragon(dragonId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the death trigger leaves the creature alone")
    void mayDestroyCanBeDeclined() {
        harness.addToBattlefield(player1, new NoxiousDragon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID dragonId = harness.getPermanentId(player1, "Noxious Dragon");

        killDragon(dragonId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature with mana value greater than 3 is not a legal target")
    void cannotTargetLargeCreature() {
        harness.addToBattlefield(player1, new NoxiousDragon());
        harness.addToBattlefield(player2, new AirElemental());
        UUID dragonId = harness.getPermanentId(player1, "Noxious Dragon");

        killDragon(dragonId);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    private void killDragon(UUID dragonId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castInstant(player2, 0, dragonId);
        harness.passBothPriorities();
    }
}
