package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.d.DarksteelPlate;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CreepingCorrosion.class, GrizzlyBears.class, HowlingMine.class, Ornithopter.class,
        DarksteelPlate.class})
class CreepingCorrosionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Creeping Corrosion puts it on the stack as a sorcery")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new CreepingCorrosion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(CreepingCorrosion.class);
    }

    @Test
    @DisplayName("Creeping Corrosion destroys artifacts controlled by both players")
    void destroysArtifactsFromBothPlayers() {
        harness.addToBattlefield(player1, new HowlingMine());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new CreepingCorrosion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Howling Mine");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Howling Mine");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Creeping Corrosion does not destroy nonartifact permanents")
    void doesNotDestroyNonArtifacts() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CreepingCorrosion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Artifacts with regeneration shields survive Creeping Corrosion")
    void regenerationShieldsProtectArtifacts() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        ornithopter.setRegenerationShield(1);

        harness.setHand(player2, List.of(new CreepingCorrosion()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Indestructible artifacts survive Creeping Corrosion")
    void indestructibleArtifactsSurvive() {
        harness.addToBattlefield(player2, new DarksteelPlate());
        harness.setHand(player1, List.of(new CreepingCorrosion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Darksteel Plate");
    }

    @Test
    @DisplayName("Creeping Corrosion resolves without artifacts on the battlefield")
    void resolvesOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new CreepingCorrosion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Creeping Corrosion");
    }
}
