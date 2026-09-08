package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoblinVandal;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FesteringEvil.class, GoblinVandal.class, PhantomWarrior.class})
class FesteringEvilTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger deals 1 damage to each creature and each player")
    void upkeepTriggerDealsOneDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FesteringEvil());
        harness.addToBattlefield(player1, new GoblinVandal());  // 1/1 dies
        harness.addToBattlefield(player2, new PhantomWarrior()); // 2/2 survives

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Vandal");
        harness.assertOnBattlefield(player2, "Phantom Warrior");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire on the opponent's upkeep")
    void upkeepTriggerDoesNotFireOnOpponentUpkeep() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FesteringEvil());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An upkeep trigger resolves even after Festering Evil leaves the battlefield")
    void upkeepTriggerResolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FesteringEvil());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Festering Evil");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("{B}{B}, Sacrifice: deals 3 damage to each creature and each player")
    void activatedAbilityDealsThreeDamageAndSacrifices() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FesteringEvil());
        harness.addToBattlefield(player2, new PhantomWarrior()); // 2/2 dies
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Festering Evil");
        harness.assertNotOnBattlefield(player2, "Phantom Warrior");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Activated ability requires two black mana")
    void activatedAbilityRequiresTwoBlackMana() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FesteringEvil());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Festering Evil");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
