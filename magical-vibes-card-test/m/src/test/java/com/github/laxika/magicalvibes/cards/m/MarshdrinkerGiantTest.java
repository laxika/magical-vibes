package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WickerboughElder;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarshdrinkerGiant.class, Island.class, Swamp.class, WickerboughElder.class})
class MarshdrinkerGiantTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB destroy trigger")
    @CardUsed({MarshdrinkerGiant.class, Island.class, Swamp.class, WickerboughElder.class})
    class EnterTheBattlefield {

        @Test
        @DisplayName("Destroys target Island an opponent controls")
        void destroysOpponentIsland() {
            harness.addToBattlefield(player2, new Island());
            castGiant(player2, "Island");
            resolveAllTriggers();

            harness.assertNotOnBattlefield(player2, "Island");
            harness.assertInGraveyard(player2, "Island");
        }

        @Test
        @DisplayName("Destroys target Swamp an opponent controls")
        void destroysOpponentSwamp() {
            harness.addToBattlefield(player2, new Swamp());
            castGiant(player2, "Swamp");
            resolveAllTriggers();

            harness.assertNotOnBattlefield(player2, "Swamp");
            harness.assertInGraveyard(player2, "Swamp");
        }

        @Test
        @DisplayName("Enters normally when only its controller has an Island or Swamp")
        void entersWithoutLegalTargets() {
            harness.addToBattlefield(player1, new Island());
            harness.addToBattlefield(player1, new Swamp());
            harness.addToBattlefield(player2, new WickerboughElder());

            harness.castFromHand(player1, new MarshdrinkerGiant(), "{3}{G}{G}");
            resolveAllTriggers();

            harness.assertOnBattlefield(player1, "Marshdrinker Giant");
            harness.assertOnBattlefield(player1, "Island");
            harness.assertOnBattlefield(player1, "Swamp");
            harness.assertOnBattlefield(player2, "Wickerbough Elder");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does not destroy another land when its target leaves before resolution")
        void targetLeavesBeforeResolution() {
            harness.addToBattlefield(player2, new Island());
            harness.addToBattlefield(player2, new Swamp());
            castGiant(player2, "Island");
            harness.passBothPriorities();
            assertThat(gd.stack).hasSize(1);

            var island = findPermanent(player2, "Island");
            gd.playerBattlefields.get(player2.getId()).remove(island);
            gd.playerHands.get(player2.getId()).add(island.getCard());
            resolveAllTriggers();

            harness.assertInHand(player2, "Island");
            harness.assertNotInGraveyard(player2, "Island");
            harness.assertOnBattlefield(player2, "Swamp");
            harness.assertOnBattlefield(player1, "Marshdrinker Giant");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does not destroy a target that its controller gains control of")
        void targetBecomesControlledByTriggerController() {
            harness.addToBattlefield(player2, new Swamp());
            castGiant(player2, "Swamp");
            harness.passBothPriorities();
            assertThat(gd.stack).hasSize(1);

            var swamp = findPermanent(player2, "Swamp");
            gd.playerBattlefields.get(player2.getId()).remove(swamp);
            gd.playerBattlefields.get(player1.getId()).add(swamp);
            resolveAllTriggers();

            harness.assertOnBattlefield(player1, "Swamp");
            harness.assertNotInGraveyard(player2, "Swamp");
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    @CardUsed({MarshdrinkerGiant.class, Island.class, WickerboughElder.class})
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
            harness.addToBattlefield(player2, new WickerboughElder());
            UUID creatureId = harness.getPermanentId(player2, "Wickerbough Elder");
            harness.setHand(player1, List.of(new MarshdrinkerGiant()));
            harness.addMana(player1, ManaColor.GREEN, 5);

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, creatureId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private void castGiant(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new MarshdrinkerGiant()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
