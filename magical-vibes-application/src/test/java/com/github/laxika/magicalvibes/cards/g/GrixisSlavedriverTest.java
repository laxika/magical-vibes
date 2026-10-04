package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrixisSlavedriver.class, Unsummon.class})
@DisplayName("Grixis Slavedriver")
class GrixisSlavedriverTest extends BaseCardTest {

    // ===== When this creature leaves the battlefield, create a 2/2 black Zombie token =====

    @Test
    @DisplayName("Leaving the battlefield (bounce) creates a 2/2 black Zombie token")
    void leavingBattlefieldCreatesZombieToken() {
        Permanent slavedriver = harness.addToBattlefieldAndReturn(player1, new GrixisSlavedriver());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, slavedriver.getId());
        harness.passBothPriorities(); // Unsummon resolves → Slavedriver returns to hand, trigger onto stack
        harness.passBothPriorities(); // leaves-battlefield trigger resolves → token created

        List<Permanent> tokens = findPermanents(player1, "Zombie");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getCard().getPower()).isEqualTo(2);
        assertThat(tokens.get(0).getCard().getToughness()).isEqualTo(2);
        assertThat(tokens.get(0).getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(tokens.get(0).getCard().isToken()).isTrue();
    }

    // ===== Unearth {3}{B} =====

    @Test
    @DisplayName("Unearth returns Grixis Slavedriver to the battlefield with haste")
    void unearthReturnsWithHaste() {
        harness.setGraveyard(player1, List.of(new GrixisSlavedriver()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Grixis Slavedriver");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Grixis Slavedriver");
    }

    @Test
    @DisplayName("Unearth-exile at the next end step still triggers the leaves-battlefield token")
    void unearthExileAtEndStepCreatesToken() {
        harness.setGraveyard(player1, List.of(new GrixisSlavedriver()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities(); // exile trigger resolves → leaves-battlefield trigger onto stack
        harness.passBothPriorities(); // leaves-battlefield trigger resolves → token created

        harness.assertNotOnBattlefield(player1, "Grixis Slavedriver");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grixis Slavedriver"));

        List<Permanent> tokens = findPermanents(player1, "Zombie");
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("Bouncing an unearthed Slavedriver exiles it and creates exactly one Zombie")
    void bouncingUnearthedSlavedriverCreatesToken() {
        harness.setGraveyard(player1, List.of(new GrixisSlavedriver()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent slavedriver = findPermanent(player1, "Grixis Slavedriver");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, slavedriver.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grixis Slavedriver");
        harness.assertNotInHand(player1, "Grixis Slavedriver");
        harness.assertNotInGraveyard(player1, "Grixis Slavedriver");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grixis Slavedriver"));
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);

        harness.passUntil(TurnStep.END_STEP);
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dying creates a Zombie token")
    void dyingCreatesZombieToken() {
        Permanent slavedriver = harness.addToBattlefieldAndReturn(player1, new GrixisSlavedriver());
        slavedriver.setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grixis Slavedriver");
        harness.assertNotOnBattlefield(player1, "Grixis Slavedriver");
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("Lethal damage to an unearthed Slavedriver exiles it and creates a Zombie")
    void lethalDamageToUnearthedSlavedriverCreatesToken() {
        harness.setGraveyard(player1, List.of(new GrixisSlavedriver()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        findPermanent(player1, "Grixis Slavedriver").setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grixis Slavedriver");
        harness.assertNotInGraveyard(player1, "Grixis Slavedriver");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grixis Slavedriver"));
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthRequiresMainPhase() {
        harness.setGraveyard(player1, List.of(new GrixisSlavedriver()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grixis Slavedriver");
        harness.assertNotOnBattlefield(player1, "Grixis Slavedriver");
    }

    @Test
    @DisplayName("Unearth requires the black mana in its activation cost")
    void unearthRequiresBlackMana() {
        harness.setGraveyard(player1, List.of(new GrixisSlavedriver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grixis Slavedriver");
        harness.assertNotOnBattlefield(player1, "Grixis Slavedriver");
    }
}
