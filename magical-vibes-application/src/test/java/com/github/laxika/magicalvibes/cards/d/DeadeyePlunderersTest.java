package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PerilousVoyage;
import com.github.laxika.magicalvibes.cards.p.PiratesCutlass;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadeyePlunderers.class, PiratesCutlass.class, PerilousVoyage.class})
class DeadeyePlunderersTest extends BaseCardTest {

    @Nested
    @CardUsed({DeadeyePlunderers.class, PiratesCutlass.class})
    @DisplayName("Static ability — +1/+1 per artifact")
    class StaticBonus {

        @Test
        void countsNontokenArtifactsButNotOtherCreatures() {
            Permanent plunderers = harness.addToBattlefieldAndReturn(player1, new DeadeyePlunderers());
            harness.addToBattlefield(player1, new PiratesCutlass());
            harness.addToBattlefield(player1, new DeadeyePlunderers());
            harness.addToBattlefield(player2, new PiratesCutlass());

            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(4);
        }

        @Test
        @DisplayName("Base stats are 3/3 with no artifacts")
        void baseStatsWithNoArtifacts() {
            Permanent plunderers = harness.addToBattlefieldAndReturn(player1, new DeadeyePlunderers());

            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(3);
        }

        @Test
        @DisplayName("Gets +1/+1 for each artifact you control")
        void boostsWithArtifacts() {
            Permanent plunderers = harness.addToBattlefieldAndReturn(player1, new DeadeyePlunderers());
            harness.addToBattlefield(player1, createArtifactToken());
            harness.addToBattlefield(player1, createArtifactToken());

            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(5);
        }

        @Test
        @DisplayName("Opponent's artifacts do not contribute to the bonus")
        void opponentArtifactsDontCount() {
            Permanent plunderers = harness.addToBattlefieldAndReturn(player1, new DeadeyePlunderers());
            harness.addToBattlefield(player2, createArtifactToken());
            harness.addToBattlefield(player2, createArtifactToken());

            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(3);
        }

        @Test
        @DisplayName("Bonus updates when an artifact leaves the battlefield")
        void bonusUpdatesWhenArtifactLeaves() {
            Permanent plunderers = harness.addToBattlefieldAndReturn(player1, new DeadeyePlunderers());
            harness.addToBattlefield(player1, createArtifactToken());
            harness.addToBattlefield(player1, createArtifactToken());

            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(5);

            gd.playerBattlefields.get(player1.getId())
                    .removeIf(p -> p.getCard().getName().equals("Test Artifact"));

            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(3);
        }
    }

    @Nested
    @CardUsed({DeadeyePlunderers.class, PerilousVoyage.class})
    @DisplayName("Activated ability — Create Treasure token")
    class TreasureAbility {

        @Test
        void canActivateRepeatedlyWhileTappedOnOpponentsTurn() {
            Permanent plunderers = harness.addToBattlefieldAndReturn(player1, new DeadeyePlunderers());
            plunderers.setTapped(true);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.addMana(player1, ManaColor.BLUE, 2);
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 4);
            harness.passPriority(player2);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.activateAbility(player1, 0, 0, null, null);

            assertThat(gd.stack).hasSize(2);
            assertThat(countPermanents(player1, "Treasure")).isZero();
            assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
            assertThat(countPermanents(player2, "Treasure")).isZero();
            assertThat(plunderers.isTapped()).isTrue();
            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(5);
        }

        @Test
        void cannotPayColoredCostWithOnlyBlueAndColorlessMana() {
            harness.addToBattlefield(player1, new DeadeyePlunderers());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(gd.stack).isEmpty();
            assertThat(countPermanents(player1, "Treasure")).isZero();
        }

        @Test
        void createsTreasureEvenAfterSourceLeavesBattlefield() {
            Permanent plunderers = harness.addToBattlefieldAndReturn(player1, new DeadeyePlunderers());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.setHand(player2, List.of(new PerilousVoyage()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 1);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, plunderers.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Deadeye Plunderers");
            assertThat(countPermanents(player1, "Treasure")).isZero();
            harness.passBothPriorities();

            assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
            assertThat(countPermanents(player2, "Treasure")).isZero();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        void tappedTreasureCannotBeSacrificedForMana() {
            harness.addToBattlefield(player1, new DeadeyePlunderers());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            Permanent treasure = findPermanent(player1, "Treasure");
            treasure.setTapped(true);

            assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(findPermanents(player1, "Treasure")).containsExactly(treasure);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        }

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

        @ParameterizedTest
        @EnumSource(value = ManaColor.class, mode = EnumSource.Mode.EXCLUDE, names = "COLORLESS")
        @DisplayName("Treasure token has sacrifice-for-mana activated ability")
        void treasureTokenHasManaAbility(ManaColor color) {
            harness.addToBattlefield(player1, new DeadeyePlunderers());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            Permanent treasure = findPermanent(player1, "Treasure");
            assertThat(treasure.isTapped()).isFalse();
            harness.activateAbility(player1, 1, 0, null, null);
            harness.handleListChoice(player1, color.name());

            harness.assertNotOnBattlefield(player1, "Treasure");
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
            Permanent plunderers = findPermanent(player1, "Deadeye Plunderers");
            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(3);
        }

        @Test
        @DisplayName("Creating Treasure boosts Deadeye Plunderers via static ability")
        void treasureBoostsPlunderers() {
            Permanent plunderers = harness.addToBattlefieldAndReturn(player1, new DeadeyePlunderers());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(3);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            // Treasure token is an artifact, so +1/+1
            assertThat(gqs.getEffectivePower(gd, plunderers)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, plunderers)).isEqualTo(4);
        }
    }

    private Card createArtifactToken() {
        Card token = new Card() {};
        token.setName("Test Artifact");
        token.setToken(true);
        token.setType(CardType.ARTIFACT);
        return token;
    }
}
