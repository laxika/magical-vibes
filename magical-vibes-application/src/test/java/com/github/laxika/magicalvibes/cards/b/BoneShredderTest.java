package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FogOfGnats;
import com.github.laxika.magicalvibes.cards.k.Knighthood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoneShredder.class, BouncingBeebles.class, FogOfGnats.class, BeastOfBurden.class,
        Knighthood.class})
class BoneShredderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield destroys a target nonartifact, nonblack creature")
    void etbDestroysTargetCreature() {
        harness.addToBattlefield(player2, new BouncingBeebles());
        harness.setHand(player1, List.of(new BoneShredder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Bouncing Beebles");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bouncing Beebles");
        harness.assertInGraveyard(player2, "Bouncing Beebles");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new FogOfGnats());
        harness.setHand(player1, List.of(new BoneShredder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Fog of Gnats");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new BeastOfBurden());
        harness.setHand(player1, List.of(new BoneShredder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Beast of Burden");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonartifact");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Knighthood());
        harness.setHand(player1, List.of(new BoneShredder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Knighthood");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
        harness.assertOnBattlefield(player2, "Knighthood");
    }

    @Test
    @DisplayName("Paying echo keeps Bone Shredder on the battlefield")
    void payingEchoKeepsBoneShredder() {
        harness.addToBattlefield(player2, new BouncingBeebles());
        harness.setHand(player1, List.of(new BoneShredder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Bouncing Beebles");
        harness.castCreature(player1, 0, targetId);
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
        harness.addToBattlefield(player2, new BouncingBeebles());
        harness.setHand(player1, List.of(new BoneShredder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Bouncing Beebles");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Bone Shredder");
    }
}
