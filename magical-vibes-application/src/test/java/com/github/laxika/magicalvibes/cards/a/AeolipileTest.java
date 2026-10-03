package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.IcatianInfantry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Aeolipile.class, IcatianInfantry.class})
class AeolipileTest extends BaseCardTest {

    @Test
    void sacrificesItselfAndDealsTwoDamageToTargetPlayer() {
        harness.addToBattlefield(player1, new Aeolipile());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Aeolipile");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void sacrificesItselfAndDealsTwoDamageToTargetCreature() {
        harness.addToBattlefield(player1, new Aeolipile());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aeolipile");
        harness.assertInGraveyard(player2, "Icatian Infantry");
    }

    @Test
    void cannotActivateWhenAlreadyTapped() {
        Permanent aeolipile = harness.addToBattlefieldAndReturn(player1, new Aeolipile());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        aeolipile.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void cannotActivateWithoutManaAndDoesNotPayOtherCosts() {
        Permanent aeolipile = harness.addToBattlefieldAndReturn(player1, new Aeolipile());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(aeolipile.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Aeolipile");
        harness.assertNotInGraveyard(player1, "Aeolipile");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItsController() {
        harness.addToBattlefield(player1, new Aeolipile());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Aeolipile");
    }

    @Test
    void cannotTargetANoncreatureArtifact() {
        harness.addToBattlefield(player1, new Aeolipile());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Aeolipile());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Aeolipile");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetLeavingBeforeResolutionDoesNotRefundSacrifice() {
        harness.addToBattlefield(player1, new Aeolipile());
        harness.addToBattlefield(player1, new Aeolipile());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Icatian Infantry");
        harness.assertNotOnBattlefield(player1, "Aeolipile");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Aeolipile"))
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
