package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RockSoldiers.class, LeoninScimitar.class, BottleGnomes.class})
class RockSoldiersTest extends BaseCardTest {

    @Test
    @DisplayName("Its entry destroys up to one target noncreature artifact")
    void entryDestroysTargetNoncreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castRockSoldiers(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Rock Soldiers");
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BottleGnomes());
        harness.setHand(player1, List.of(new RockSoldiers()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature artifact");
    }

    @Test
    @DisplayName("Can enter without choosing a target")
    void canEnterWithoutTarget() {
        castRockSoldiers(null);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rock Soldiers");
    }

    private void castRockSoldiers(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new RockSoldiers()));
        harness.addMana(player1, ManaColor.RED, 4);
        if (targetId == null) {
            harness.castCreature(player1, 0);
        } else {
            harness.castCreature(player1, 0, 0, targetId);
        }
    }
}
