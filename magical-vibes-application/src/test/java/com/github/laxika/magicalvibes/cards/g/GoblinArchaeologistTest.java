package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DragonsClaw;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinArchaeologist.class, DragonsClaw.class, CrazedGoblin.class})
class GoblinArchaeologistTest extends BaseCardTest {

    @Test
    @DisplayName("Flips a coin and applies the matching artifact-removal outcome")
    void flipsCoinAndAppliesMatchingOutcome() {
        Permanent archaeologist = addCreatureReady(player1, new GoblinArchaeologist());
        Permanent artifact = addCreatureReady(player2, new DragonsClaw());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        assertThat(archaeologist.isTapped()).isTrue();

        harness.passBothPriorities();

        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        assertThat(logs).anyMatch(log -> log.contains("coin flip for Goblin Archaeologist"));

        boolean wonFlip = logs.stream()
                .anyMatch(log -> log.contains("wins the coin flip for Goblin Archaeologist"));
        if (wonFlip) {
            harness.assertNotOnBattlefield(player2, "Dragon's Claw");
            harness.assertInGraveyard(player2, "Dragon's Claw");
            assertThat(archaeologist.isTapped()).isFalse();
            harness.assertOnBattlefield(player1, "Goblin Archaeologist");
        } else {
            harness.assertOnBattlefield(player2, "Dragon's Claw");
            harness.assertInGraveyard(player1, "Goblin Archaeologist");
        }
    }

    @Test
    @DisplayName("Can target only artifacts")
    void canTargetOnlyArtifacts() {
        addCreatureReady(player1, new GoblinArchaeologist());
        Permanent creature = addCreatureReady(player2, new CrazedGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
