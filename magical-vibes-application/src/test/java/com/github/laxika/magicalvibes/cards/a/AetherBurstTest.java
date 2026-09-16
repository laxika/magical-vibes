package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RabidElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherBurst.class, Island.class, RabidElephant.class})
class AetherBurstTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one plus the Aether Burst cards in all graveyards")
    void returnsUpToGraveyardCountAcrossAllGraveyards() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player2, new RabidElephant());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player2, new RabidElephant());
        Permanent creature3 = harness.addToBattlefieldAndReturn(player2, new RabidElephant());
        Permanent creature4 = harness.addToBattlefieldAndReturn(player2, new RabidElephant());
        harness.setGraveyard(player1, List.of(new AetherBurst(), new Island()));
        harness.setGraveyard(player2, List.of(new AetherBurst(), new AetherBurst()));
        harness.setHand(player1, List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0,
                List.of(creature1.getId(), creature2.getId(), creature3.getId(), creature4.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Rabid Elephant"))
                .hasSize(4);
    }

    @Test
    @DisplayName("Cannot choose more targets than the cast-time graveyard count allows")
    void rejectsTooManyTargets() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player2, new RabidElephant());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player2, new RabidElephant());
        Permanent creature3 = harness.addToBattlefieldAndReturn(player2, new RabidElephant());
        harness.setGraveyard(player1, List.of(new AetherBurst(), new Island()));
        harness.setHand(player1, List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature1.getId(), creature2.getId(), creature3.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Allows choosing no creatures")
    void allowsChoosingNoCreatures() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Aether Burst");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNonCreatureTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
