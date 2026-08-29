package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoneShredderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield destroys a target nonartifact, nonblack creature")
    void etbDestroysTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoneShredder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new BoneShredder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Mass of Ghouls");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.setHand(player1, List.of(new BoneShredder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Bottle Gnomes");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonartifact");
    }

    @Test
    @DisplayName("Paying echo keeps Bone Shredder on the battlefield")
    void payingEchoKeepsBoneShredder() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoneShredder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Bone Shredder");
    }

    @Test
    @DisplayName("Declining echo sacrifices Bone Shredder")
    void decliningEchoSacrificesBoneShredder() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoneShredder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Bone Shredder");
    }
}
