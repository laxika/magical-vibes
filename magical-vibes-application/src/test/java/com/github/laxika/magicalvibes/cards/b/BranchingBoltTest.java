package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BranchingBoltTest extends BaseCardTest {

    private void giveMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private UUID battlefieldId(com.github.laxika.magicalvibes.model.Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .map(Permanent::getId)
                .findFirst().orElseThrow();
    }

    // ===== Mode 0 — creature with flying =====

    @Test
    @DisplayName("Mode 0 deals 3 damage to a creature with flying, killing it")
    void mode0KillsFlyingCreature() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        harness.castInstant(player1, 0, 0, battlefieldId(player2, "Suntail Hawk"));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Suntail Hawk"));
    }

    @Test
    @DisplayName("Mode 0 cannot target a creature without flying")
    void mode0CannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk()); // valid target so spell is castable
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        UUID bearsId = battlefieldId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Mode 1 — creature without flying =====

    @Test
    @DisplayName("Mode 1 deals 3 damage to a creature without flying, killing it")
    void mode1KillsNonFlyingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        harness.castInstant(player1, 0, 1, battlefieldId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Mode 1 cannot target a creature with flying")
    void mode1CannotTargetFlyingCreature() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new GrizzlyBears()); // valid target so spell is castable
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        UUID hawkId = battlefieldId(player2, "Suntail Hawk");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, hawkId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Mode 2 — both =====

    @Test
    @DisplayName("Mode 2 deals 3 damage to both a flying and a non-flying creature, killing both")
    void mode2KillsBothCreatures() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        UUID hawkId = battlefieldId(player2, "Suntail Hawk");
        UUID bearsId = battlefieldId(player2, "Grizzly Bears");
        harness.castModalInstant(player1, 0, 2, List.of(hawkId, bearsId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Suntail Hawk"))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Branching Bolt goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        harness.castInstant(player1, 0, 0, battlefieldId(player2, "Suntail Hawk"));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Branching Bolt"));
    }
}
