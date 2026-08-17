package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EssenceFeedTest extends BaseCardTest {

    @Test
    @DisplayName("Target player loses 3 life, you gain 3 life, and three Eldrazi Spawn tokens are created")
    void drainsLifeAndCreatesSpawnTokens() {
        harness.setLife(player1, 17);
        harness.setLife(player2, 20);
        castAndResolve(player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(3);
    }

    @Test
    @DisplayName("An Eldrazi Spawn created by Essence Feed can be sacrificed for colorless mana")
    void spawnSacrificeAddsColorlessMana() {
        castAndResolve(player2.getId());

        Permanent spawn = findPermanents(player1, "Eldrazi Spawn").getFirst();
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
    }

    @Test
    @DisplayName("Essence Feed cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EssenceFeed()));
        addEssenceFeedMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAndResolve(UUID targetId) {
        harness.setHand(player1, List.of(new EssenceFeed()));
        addEssenceFeedMana();

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addEssenceFeedMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
