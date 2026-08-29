package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkullCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep only offers black creatures the controller controls")
    void upkeepOnlyOffersControlledBlackCreatures() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new SkullCollector());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player1, new ScatheZombies());
        Permanent nonblackCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBlackCreature = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(collector.getId(), blackCreature.getId())
                .doesNotContain(nonblackCreature.getId(), opponentBlackCreature.getId());
    }

    @Test
    @DisplayName("The chosen black creature returns to its owner's hand")
    void chosenBlackCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new SkullCollector());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player1, new ScatheZombies());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blackCreature.getId());

        harness.assertNotOnBattlefield(player1, "Scathe Zombies");
        harness.assertInHand(player1, "Scathe Zombies");
        harness.assertOnBattlefield(player1, "Skull Collector");
    }

    @Test
    @DisplayName("The upkeep trigger returns Skull Collector when it is the only black creature")
    void returnsItselfWhenOnlyBlackCreature() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new SkullCollector());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, collector.getId());

        harness.assertNotOnBattlefield(player1, "Skull Collector");
        harness.assertInHand(player1, "Skull Collector");
    }

    @Test
    @DisplayName("The activated ability grants a regeneration shield")
    void activatedAbilityGrantsRegenerationShield() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new SkullCollector());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(collector.getRegenerationShield()).isEqualTo(1);
    }
}
