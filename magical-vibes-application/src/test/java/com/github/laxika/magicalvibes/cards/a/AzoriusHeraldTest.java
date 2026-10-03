package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzoriusHerald.class})
class AzoriusHeraldTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 4 life and stays on the battlefield when blue mana was spent")
    void gainsLifeAndStaysWhenBlueManaWasSpent() {
        harness.setHand(player1, List.of(new AzoriusHerald()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        harness.assertOnBattlefield(player1, "Azorius Herald");
    }

    @Test
    @DisplayName("Gains 4 life and is sacrificed when blue mana was not spent")
    void gainsLifeAndIsSacrificedWithoutBlueMana() {
        harness.setHand(player1, List.of(new AzoriusHerald()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        harness.assertNotOnBattlefield(player1, "Azorius Herald");
        harness.assertInGraveyard(player1, "Azorius Herald");
    }

    @Test
    @DisplayName("Is sacrificed when blue mana is available but not spent")
    void isSacrificedWhenBlueManaIsNotSpent() {
        harness.setHand(player1, List.of(new AzoriusHerald()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        harness.assertNotOnBattlefield(player1, "Azorius Herald");
        harness.assertInGraveyard(player1, "Azorius Herald");
    }

    @Test
    @DisplayName("Life gain and sacrifice are separate triggered abilities")
    void entersAbilitiesResolveSeparately() {
        harness.setHand(player1, List.of(new AzoriusHerald()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Azorius Herald");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        boolean lifeGainResolved = gd.playerLifeTotals.get(player1.getId()) == 24;
        boolean sacrificeResolved = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(card -> card instanceof AzoriusHerald);
        assertThat(lifeGainResolved ^ sacrificeResolved).isTrue();

        resolveAllTriggers();

        harness.assertLife(player1, 24);
        harness.assertNotOnBattlefield(player1, "Azorius Herald");
        harness.assertInGraveyard(player1, "Azorius Herald");
    }

    @Test
    @DisplayName("Entering without being cast gains life and sacrifices the Herald despite available blue mana")
    void enteringWithoutCastingGainsLifeAndSacrifices() {
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.enterBattlefieldAndReturn(player2, new AzoriusHerald());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 24);
        harness.assertNotOnBattlefield(player2, "Azorius Herald");
        harness.assertInGraveyard(player2, "Azorius Herald");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be blocked")
    void cannotBeBlocked() {
        Permanent herald = addCreatureReady(player1, new AzoriusHerald());
        herald.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AzoriusHerald());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(herald)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
