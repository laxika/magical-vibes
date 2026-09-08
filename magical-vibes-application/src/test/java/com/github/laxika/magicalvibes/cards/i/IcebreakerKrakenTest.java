package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IcebreakerKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for snow lands reduces the generic mana cost")
    void affinityForSnowLandsReducesGenericCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new SnowCoveredIsland());
        }
        harness.setHand(player1, List.of(new IcebreakerKraken()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only snow lands controlled by the spell's controller")
    void affinityCountsOnlyControlledSnowLands() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new SnowCoveredIsland());
        }
        harness.setHand(player1, List.of(new IcebreakerKraken()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The enters-the-battlefield ability keeps target opponent's artifacts and creatures tapped")
    void locksTargetOpponentsArtifactsAndCreatures() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        artifact.tap();
        creature.tap();
        land.tap();

        castKrakenTargeting(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(artifact.getSkipUntapCount()).isEqualTo(1);
        assertThat(creature.getSkipUntapCount()).isEqualTo(1);
        assertThat(land.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Returning three snow lands as a cost returns Icebreaker Kraken to its owner's hand")
    void returnsThreeSnowLandsAndItself() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new IcebreakerKraken());
        Permanent snowLand1 = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());
        Permanent snowLand2 = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());
        Permanent snowLand3 = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());
        Permanent nonSnowLand = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(nonSnowLand);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Icebreaker Kraken"))
                .count()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Snow-Covered Island"))
                .count()).isEqualTo(3);
        assertThat(kraken).isNotIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(snowLand1).isNotIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(snowLand2).isNotIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(snowLand3).isNotIn(gd.playerBattlefields.get(player1.getId()));
    }

    private void castKrakenTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new IcebreakerKraken()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0, 0, targetPlayerId);
    }
}
