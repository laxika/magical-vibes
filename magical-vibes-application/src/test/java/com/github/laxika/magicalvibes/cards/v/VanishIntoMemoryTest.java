package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FrostwebSpider;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VanishIntoMemory.class, FrostwebSpider.class, SnowCoveredIsland.class})
class VanishIntoMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws using pre-exile power and discards using returned toughness")
    void usesPowerBeforeExileAndToughnessAfterReturn() {
        harness.addToBattlefield(player2, new FrostwebSpider());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(
                new VanishIntoMemory(), new SnowCoveredIsland(), new SnowCoveredIsland(),
                new SnowCoveredIsland(), new SnowCoveredIsland(), new SnowCoveredIsland()));
        addVanishMana();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 2);
        harness.assertNotOnBattlefield(player2, "Frostweb Spider");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Frostweb Spider"));

        advanceToUpkeep(player2);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Frostweb Spider"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player2, "Frostweb Spider");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Returns a stolen creature to its owner")
    void returnsStolenCreatureToOwner() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrostwebSpider());
        gd.stolenCreatures.put(target.getId(), player2.getId());

        harness.setHand(player1, List.of(
                new VanishIntoMemory(), new SnowCoveredIsland(), new SnowCoveredIsland(),
                new SnowCoveredIsland(), new SnowCoveredIsland()));
        addVanishMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        harness.assertNotOnBattlefield(player1, "Frostweb Spider");
        harness.assertOnBattlefield(player2, "Frostweb Spider");

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new SnowCoveredIsland());
        harness.setHand(player1, List.of(new VanishIntoMemory()));
        addVanishMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Snow-Covered Island")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addVanishMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

}
