package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScaldingCauldron.class, AirElemental.class, Gingerbrute.class})
class ScaldingCauldronTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target creature")
    void dealsThreeDamageToTargetCreature() {
        harness.addToBattlefield(player1, new ScaldingCauldron());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Is sacrificed as the ability's activation cost")
    void isSacrificedAsCost() {
        harness.addToBattlefield(player1, new ScaldingCauldron());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Scalding Cauldron");
        harness.assertInGraveyard(player1, "Scalding Cauldron");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new ScaldingCauldron());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lethal damage destroys the targeted creature after the source is sacrificed")
    void lethalDamageDestroysTarget() {
        harness.addToBattlefield(player1, new ScaldingCauldron());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scalding Cauldron");
        harness.assertNotOnBattlefield(player2, "Gingerbrute");
        harness.assertInGraveyard(player2, "Gingerbrute");
    }

    @Test
    @DisplayName("Cannot activate with only two mana and does not sacrifice the source")
    void insufficientManaDoesNotPayCosts() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new ScaldingCauldron());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cauldron.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Scalding Cauldron");
        harness.assertNotInGraveyard(player1, "Scalding Cauldron");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate a tapped Cauldron")
    void cannotActivateWhenTapped() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new ScaldingCauldron());
        cauldron.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Scalding Cauldron");
        harness.assertNotInGraveyard(player1, "Scalding Cauldron");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target a creature controlled by the ability's controller")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new ScaldingCauldron());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gingerbrute");
        harness.assertNotOnBattlefield(player1, "Gingerbrute");
    }

    @Test
    @DisplayName("Does not deal damage when the target is sacrificed in response")
    void targetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new ScaldingCauldron());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Scalding Cauldron");
        harness.assertInGraveyard(player2, "Gingerbrute");
        harness.assertLife(player2, 23);
        assertThat(gd.stack).isEmpty();
    }
}
