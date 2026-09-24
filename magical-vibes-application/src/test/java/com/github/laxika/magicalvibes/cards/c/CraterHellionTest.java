package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.s.ShivanHellkite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CraterHellion.class, GorillaWarrior.class, ShivanHellkite.class})
class CraterHellionTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 4 damage to each other creature and spares Crater Hellion")
    void etbDamagesEachOtherCreature() {
        harness.addToBattlefield(player1, new GorillaWarrior());
        harness.addToBattlefield(player2, new GorillaWarrior());
        harness.addToBattlefield(player2, new ShivanHellkite());

        castAndResolveCraterHellion();

        harness.assertNotOnBattlefield(player1, "Gorilla Warrior");
        harness.assertNotOnBattlefield(player2, "Gorilla Warrior");
        harness.assertOnBattlefield(player1, "Crater Hellion");
        assertThat(findPermanent(player1, "Crater Hellion").getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Shivan Hellkite").getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Unpaid echo sacrifices Crater Hellion at the next upkeep")
    void unpaidEchoSacrificesCraterHellion() {
        castAndResolveCraterHellion();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Crater Hellion");
        harness.assertInGraveyard(player1, "Crater Hellion");
    }

    @Test
    @DisplayName("Paying echo keeps Crater Hellion and echo does not trigger again")
    void payingEchoKeepsCraterHellionAndIsOneShot() {
        castAndResolveCraterHellion();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Crater Hellion");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Crater Hellion");
    }

    private void castAndResolveCraterHellion() {
        harness.castFromHand(player1, new CraterHellion(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
