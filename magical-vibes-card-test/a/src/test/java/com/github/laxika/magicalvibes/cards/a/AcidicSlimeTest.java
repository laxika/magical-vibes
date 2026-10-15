package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcidicSlime.class, Forest.class, HonorOfThePure.class, RuneclawBear.class, RodOfRuin.class, CrawWurm.class})
class AcidicSlimeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can destroy a land controlled by its controller")
    void etbDestroysOwnLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0, harness.getPermanentId(player1, "Forest"));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Acidic Slime");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deathtouch destroys a blocker with more toughness than Slime's power")
    void deathtouchDestroysLargerBlocker() {
        addCreatureReady(player1, new AcidicSlime());
        harness.addToBattlefield(player2, new CrawWurm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Craw Wurm");
        harness.assertInGraveyard(player2, "Craw Wurm");
        harness.assertInGraveyard(player1, "Acidic Slime");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB destroys target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Acidic Slime");
        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("ETB destroys target enchantment")
    void etbDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new HonorOfThePure());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID targetId = harness.getPermanentId(player2, "Honor of the Pure");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Acidic Slime");
        harness.assertNotOnBattlefield(player2, "Honor of the Pure");
        harness.assertInGraveyard(player2, "Honor of the Pure");
    }

    @Test
    @DisplayName("ETB destroys target land")
    void etbDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Acidic Slime");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a creature that is not an artifact, enchantment, or land")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or land");
    }

    @Test
    @DisplayName("ETB fizzles if target is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell Ă˘â€ â€™ ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB Ă˘â€ â€™ fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Can cast without a target when no valid targets on battlefield")
    void canCastWithoutTargetWhenNoValidTargets() {
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Acidic Slime");
    }

    @Test
    @DisplayName("ETB is not put on the stack when no legal target exists")
    void etbIsNotPutOnStackWithoutLegalTargets() {
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Acidic Slime");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can cast without target when only creatures exist")
    void canCastWithoutTargetWhenOnlyCreaturesExist() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Acidic Slime");
    }
}
