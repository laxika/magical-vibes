package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarshdrinkerGiantTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB destroy trigger")
    class EnterTheBattlefield {

        @Test
        @DisplayName("Destroys target Island an opponent controls")
        void destroysOpponentIsland() {
            harness.addToBattlefield(player2, new Island());
            castGiant(player2, "Island");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            harness.assertNotOnBattlefield(player2, "Island");
            harness.assertInGraveyard(player2, "Island");
        }

        @Test
        @DisplayName("Destroys target Swamp an opponent controls")
        void destroysOpponentSwamp() {
            harness.addToBattlefield(player2, new Swamp());
            castGiant(player2, "Swamp");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            harness.assertNotOnBattlefield(player2, "Swamp");
            harness.assertInGraveyard(player2, "Swamp");
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot target an Island you control")
        void cannotTargetOwnIsland() {
            harness.addToBattlefield(player1, new Island());
            UUID ownIslandId = harness.getPermanentId(player1, "Island");
            harness.setHand(player1, List.of(new MarshdrinkerGiant()));
            harness.addMana(player1, ManaColor.GREEN, 5);

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownIslandId, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target an opponent's non-Island/Swamp permanent")
        void cannotTargetOtherPermanent() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.setHand(player1, List.of(new MarshdrinkerGiant()));
            harness.addMana(player1, ManaColor.GREEN, 5);

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bearsId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ===== Helpers =====

    private void castGiant(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new MarshdrinkerGiant()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
