package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RapaciousDragon.class})
class RapaciousDragonTest extends BaseCardTest {

    @Test
    @DisplayName("When Rapacious Dragon enters, two Treasure tokens are created")
    void etbCreatesTwoTreasureTokens() {
        harness.setHand(player1, List.of(new RapaciousDragon()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(2);
        harness.assertOnBattlefield(player1, "Rapacious Dragon");
    }

    @Test
    @DisplayName("Treasures are created only when the enter trigger resolves")
    void treasuresWaitForTriggerResolution() {
        harness.setHand(player1, List.of(new RapaciousDragon()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Rapacious Dragon");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The dragon's controller receives both Treasures")
    void opponentReceivesTheirOwnTreasures() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new RapaciousDragon()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("A fresh Treasure can be sacrificed immediately for any color of mana")
    void treasureProducesChosenColorImmediately(ManaColor color) {
        harness.setHand(player1, List.of(new RapaciousDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(treasure);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
