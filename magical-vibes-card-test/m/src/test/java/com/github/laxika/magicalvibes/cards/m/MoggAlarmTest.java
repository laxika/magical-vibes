package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.t.TerrainGenerator;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoggAlarm.class, Mountain.class, TerrainGenerator.class})
class MoggAlarmTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 1/1 red Goblin tokens when cast for mana")
    void createsTwoGoblinTokens() {
        harness.castFromHand(player1, new MoggAlarm(), "{1}{R}{R}");
        harness.passBothPriorities();

        List<Permanent> goblins = findGoblinTokens();
        assertThat(goblins).hasSize(2);
        for (Permanent goblin : goblins) {
            assertThat(goblin.getCard().getPower()).isEqualTo(1);
            assertThat(goblin.getCard().getToughness()).isEqualTo(1);
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(goblin.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        }
    }

    @Test
    @DisplayName("Can be cast by sacrificing two Mountains instead of paying mana")
    void castsBySacrificingTwoMountains() {
        UUID mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        UUID mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setHand(player1, List.of(new MoggAlarm()));

        harness.castWithAlternateCost(player1, 0, List.of(mountain1, mountain2));
        harness.passBothPriorities();

        assertThat(findGoblinTokens()).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Mountain"))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Mogg Alarm");
    }

    @Test
    @DisplayName("Alternate cost requires two Mountains")
    void alternateCostRequiresTwoMountains() {
        UUID mountain = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setHand(player1, List.of(new MoggAlarm()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(mountain)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertInHand(player1, "Mogg Alarm");
    }

    @Test
    @DisplayName("Alternate cost rejects a non-Mountain")
    void alternateCostRejectsNonMountain() {
        Permanent nonMountain1 = harness.addToBattlefieldAndReturn(player1, new TerrainGenerator());
        Permanent nonMountain2 = harness.addToBattlefieldAndReturn(player1, new TerrainGenerator());
        harness.setHand(player1, List.of(new MoggAlarm()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(nonMountain1.getId(), nonMountain2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Terrain Generator")).hasSize(2);
        harness.assertInHand(player1, "Mogg Alarm");
    }

    @Test
    @DisplayName("Alternate cost cannot sacrifice Mountains controlled by the opponent")
    void alternateCostCannotSacrificeOpponentsMountains() {
        Permanent opponentMountain1 = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent opponentMountain2 = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new MoggAlarm()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(opponentMountain1.getId(), opponentMountain2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player2, "Mountain")).hasSize(2);
        harness.assertInHand(player1, "Mogg Alarm");
    }

    @Test
    @DisplayName("Tapped Mountains can pay the alternate cost before tokens are created")
    void tappedMountainsAreSacrificedBeforeResolution() {
        Permanent mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        mountain1.tap();
        mountain2.tap();
        harness.setHand(player1, List.of(new MoggAlarm()));

        harness.castWithAlternateCost(player1, 0, List.of(mountain1.getId(), mountain2.getId()));

        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Mountain"))
                .hasSize(2);
        assertThat(findGoblinTokens()).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInHand(player1, "Mogg Alarm");

        harness.passBothPriorities();

        assertThat(findGoblinTokens()).hasSize(2);
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
        harness.assertInGraveyard(player1, "Mogg Alarm");
    }

    @Test
    @DisplayName("The same Mountain cannot be sacrificed twice for the alternate cost")
    void alternateCostRejectsDuplicateMountain() {
        Permanent mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new MoggAlarm()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(mountain1.getId(), mountain1.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Mountain")).hasSize(2);
        harness.assertNotInGraveyard(player1, "Mountain");
        harness.assertInHand(player1, "Mogg Alarm");
        assertThat(gd.stack).isEmpty();
        assertThat(findGoblinTokens()).isEmpty();
    }

    private List<Permanent> findGoblinTokens() {
        return findPermanents(player1, "Goblin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
