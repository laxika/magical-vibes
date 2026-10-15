package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GideonJura;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.z.ZulaportEnforcer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Magmaw.class, PropheticPrism.class, ZulaportEnforcer.class, Mountain.class, GideonJura.class})
class MagmawTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 1 damage to target player")
    void sacrificesItselfAndDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new Magmaw());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Magmaw");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sacrifices a chosen nonland permanent and deals damage to target creature")
    void sacrificesChosenNonlandPermanentAndDealsDamageToCreature() {
        harness.addToBattlefield(player1, new Magmaw());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player2, new ZulaportEnforcer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID prismId = harness.getPermanentId(player1, "Prophetic Prism");
        UUID enforcerId = harness.getPermanentId(player2, "Zulaport Enforcer");

        harness.activateAbility(player1, 0, null, enforcerId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, prismId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Prophetic Prism");
        harness.assertNotOnBattlefield(player2, "Zulaport Enforcer");
        harness.assertOnBattlefield(player1, "Magmaw");
    }

    @Test
    @DisplayName("Does not sacrifice a land when a nonland source is available")
    void doesNotSacrificeLand() {
        harness.addToBattlefield(player1, new Magmaw());
        harness.addToBattlefield(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Magmaw");
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    void canActivateWhileTappedAndSummoningSickAndTargetItsController() {
        var magmaw = harness.addToBattlefieldAndReturn(player1, new Magmaw());
        magmaw.tap();
        magmaw.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());

        harness.assertInGraveyard(player1, "Magmaw");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
    }

    @Test
    void canTargetItselfAndSacrificeAnotherPermanent() {
        var magmaw = harness.addToBattlefieldAndReturn(player1, new Magmaw());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, magmaw.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Prophetic Prism"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Prophetic Prism");
        harness.assertOnBattlefield(player1, "Magmaw");
        assertThat(magmaw.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void sacrificedTargetIsGoneBeforeResolution() {
        harness.addToBattlefield(player1, new Magmaw());
        harness.addToBattlefield(player1, new ZulaportEnforcer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player1, "Zulaport Enforcer");

        harness.activateAbility(player1, 0, null, targetId);
        harness.handlePermanentChosen(player1, targetId);

        harness.assertInGraveyard(player1, "Zulaport Enforcer");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Magmaw");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new Magmaw());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Magmaw");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetANoncreatureArtifact() {
        harness.addToBattlefield(player1, new Magmaw());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player2, "Prophetic Prism");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Magmaw");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dealsDamageToAPlaneswalkerAfterSacrificingItself() {
        harness.addToBattlefield(player1, new Magmaw());
        var gideon = harness.addToBattlefieldAndReturn(player2, new GideonJura());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int loyaltyBefore = gideon.getCounterCount(CounterType.LOYALTY);

        harness.activateAbility(player1, 0, null, gideon.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Magmaw");
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 1);
    }
}
