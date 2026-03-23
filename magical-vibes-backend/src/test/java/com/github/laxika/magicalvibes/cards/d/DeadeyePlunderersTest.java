package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeadeyePlunderersTest extends BaseCardTest {

    // ===== Static ability: +1/+1 per artifact =====

    @Nested
    @DisplayName("Static ability — +1/+1 per artifact")
    class StaticBonus {

        @Test
        @DisplayName("Base stats are 3/3 with no artifacts")
        void baseStatsWithNoArtifacts() {
            harness.addToBattlefield(player1, new DeadeyePlunderers());

            Permanent plunderers = findPermanent(player1, "Deadeye Plunderers");
            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(3);
        }

        @Test
        @DisplayName("Gets +1/+1 for each artifact you control")
        void boostsWithArtifacts() {
            harness.addToBattlefield(player1, new DeadeyePlunderers());
            harness.addToBattlefield(player1, createArtifactToken());
            harness.addToBattlefield(player1, createArtifactToken());

            Permanent plunderers = findPermanent(player1, "Deadeye Plunderers");
            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(5);
        }

        @Test
        @DisplayName("Opponent's artifacts do not contribute to the bonus")
        void opponentArtifactsDontCount() {
            harness.addToBattlefield(player1, new DeadeyePlunderers());
            harness.addToBattlefield(player2, createArtifactToken());
            harness.addToBattlefield(player2, createArtifactToken());

            Permanent plunderers = findPermanent(player1, "Deadeye Plunderers");
            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(3);
        }

        @Test
        @DisplayName("Bonus updates when an artifact leaves the battlefield")
        void bonusUpdatesWhenArtifactLeaves() {
            harness.addToBattlefield(player1, new DeadeyePlunderers());
            harness.addToBattlefield(player1, createArtifactToken());
            harness.addToBattlefield(player1, createArtifactToken());

            Permanent plunderers = findPermanent(player1, "Deadeye Plunderers");
            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(5);

            gd.playerBattlefields.get(player1.getId())
                    .removeIf(p -> p.getCard().getName().equals("Test Artifact"));

            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(3);
        }
    }

    // ===== Activated ability: Create Treasure token =====

    @Nested
    @DisplayName("Activated ability — Create Treasure token")
    class TreasureAbility {

        @Test
        @DisplayName("Activating ability creates a Treasure artifact token")
        void createsATreasureToken() {
            harness.addToBattlefield(player1, new DeadeyePlunderers());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            Permanent treasure = findPermanent(player1, "Treasure");
            assertThat(treasure).isNotNull();
            assertThat(treasure.getCard().isToken()).isTrue();
            assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        }

        @Test
        @DisplayName("Treasure token has sacrifice-for-mana activated ability")
        void treasureTokenHasManaAbility() {
            harness.addToBattlefield(player1, new DeadeyePlunderers());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            Permanent treasure = findPermanent(player1, "Treasure");
            assertThat(treasure.getCard().getActivatedAbilities()).hasSize(1);
            assertThat(treasure.getCard().getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        }

        @Test
        @DisplayName("Creating Treasure boosts Deadeye Plunderers via static ability")
        void treasureBoostsPlunderers() {
            harness.addToBattlefield(player1, new DeadeyePlunderers());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            Permanent plunderers = findPermanent(player1, "Deadeye Plunderers");
            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(3);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            // Treasure token is an artifact, so +1/+1
            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(4);
        }
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }

    private Card createArtifactToken() {
        Card token = new Card() {};
        token.setName("Test Artifact");
        token.setToken(true);
        token.setType(CardType.ARTIFACT);
        return token;
    }
}
