package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FemerefArchers;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessEmbermage.class, FemerefArchers.class, FeralShadow.class})
class RecklessEmbermageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 to a target player and 1 to itself; the 2/2 survives")
    void dealsOneToPlayerAndSelf() {
        Permanent embermage = addCreatureReady(player1, new RecklessEmbermage());
        addRedMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(embermage.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Reckless Embermage");
    }

    @Test
    @DisplayName("Can target a creature — deals 1 damage to it")
    void dealsOneToTargetCreature() {
        addCreatureReady(player1, new RecklessEmbermage());
        addRedMana(player1);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new FemerefArchers());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // 1 damage marked on the 2/2 Archers — it survives.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Femeref Archers");
    }

    @Test
    @DisplayName("Lethal damage to the target creature does not stop the self-damage")
    void lethalTargetDoesNotStopSelfDamage() {
        Permanent embermage = addCreatureReady(player1, new RecklessEmbermage());
        addRedMana(player1);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new FeralShadow());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Feral Shadow");
        assertThat(embermage.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Reckless Embermage");
    }

    @Test
    @DisplayName("Ability has no tap cost — repeating it twice kills the Embermage")
    void repeatedActivationsKillItself() {
        addCreatureReady(player1, new RecklessEmbermage());
        addRedMana(player1);
        addRedMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Two points of self-damage on the 2/2 send it to the graveyard.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Reckless Embermage");
        harness.assertInGraveyard(player1, "Reckless Embermage");
    }

    private void addRedMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

}
