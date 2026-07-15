package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GoblinEliteInfantry;
import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NamelessInversionTest extends BaseCardTest {

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    // ===== +3/-3 =====

    @Test
    @DisplayName("Gives target creature +3/-3")
    void appliesBoost() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental()); // 4/4
        castOn(elemental);

        assertThat(elemental.getPowerModifier()).isEqualTo(3);
        assertThat(elemental.getToughnessModifier()).isEqualTo(-3);
        assertThat(elemental.getEffectivePower()).isEqualTo(7);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("-3 toughness kills a small creature")
    void killsSmallCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2
        castOn(bears);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Loses all creature types =====

    @Test
    @DisplayName("Strips creature types, removing a tribal buff")
    void losesAllCreatureTypes() {
        harness.addToBattlefield(player1, new GoblinKing()); // Goblins get +1/+1
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinEliteInfantry()); // 2/2 -> 3/3

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3); // buffed by Goblin King

        castOn(goblin);

        // Base 2/2, loses Goblin type (King buff gone), then +3/-3 => 5/-1 => dies.
        assertThat(goblin.isLosesAllCreatureTypesUntilEndOfTurn()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Elite Infantry"));
    }

    // ===== Wears off =====

    @Test
    @DisplayName("+3/-3 and type loss wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castOn(elemental);
        assertThat(elemental.getPowerModifier()).isEqualTo(3);
        assertThat(elemental.isLosesAllCreatureTypesUntilEndOfTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(0);
        assertThat(elemental.getToughnessModifier()).isEqualTo(0);
        assertThat(elemental.isLosesAllCreatureTypesUntilEndOfTurn()).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID targetId = bears.getId();
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
