package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PestilenceDemon.class, FugitiveWizard.class, GrizzlyBears.class})
class PestilenceDemonTest extends BaseCardTest {

    @Test
    @DisplayName("{B}: deals 1 damage to each creature and each player")
    void activatedAbilityDealsOneDamageToEachCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PestilenceDemon());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pestilence Demon");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Demon can activate repeatedly and damages itself")
    void tappedDemonCanActivateRepeatedlyAndDamagesItself() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PestilenceDemon());
        harness.addToBattlefield(player2, new PestilenceDemon());
        var demon = findPermanent(player1, "Pestilence Demon");
        demon.tap();
        demon.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(demon.isTapped()).isTrue();
        assertThat(demon.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Pestilence Demon").getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Queued activations resolve even after lethal damage kills the Demon")
    void queuedActivationResolvesAfterSourceDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PestilenceDemon());
        harness.addMana(player1, ManaColor.BLACK, 7);

        for (int i = 0; i < 7; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Pestilence Demon");
        harness.assertInGraveyard(player1, "Pestilence Demon");
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 13);
    }
}
