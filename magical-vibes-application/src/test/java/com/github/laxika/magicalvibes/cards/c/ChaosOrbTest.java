package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosOrb.class, Disenchant.class, GrizzlyBears.class})
class ChaosOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys itself while sparing tokens")
    void destroysItselfWhileSparingTokens() {
        harness.addToBattlefield(player1, new ChaosOrb());

        Card token = new GrizzlyBears();
        token.setToken(true);
        harness.addToBattlefield(player2, token);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chaos Orb");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The source's destruction respects regeneration")
    void sourceDestructionRespectsRegeneration() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ChaosOrb());
        orb.setRegenerationShield(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chaos Orb");
        harness.assertNotInGraveyard(player1, "Chaos Orb");
    }

    @Test
    @DisplayName("Does nothing when Chaos Orb leaves before its ability resolves")
    void doesNothingWhenSourceLeavesBattlefield() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ChaosOrb());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, orb.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chaos Orb");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Activation taps Chaos Orb and uses the stack before destroying it")
    void activationTapsSourceBeforeResolution() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ChaosOrb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(orb.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Chaos Orb");
        harness.assertNotInGraveyard(player1, "Chaos Orb");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chaos Orb");
    }

    @Test
    @DisplayName("Cannot activate Chaos Orb without paying one mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new ChaosOrb());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Chaos Orb");
    }

    @Test
    @DisplayName("Cannot activate an already tapped Chaos Orb")
    void cannotActivateTappedSource() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ChaosOrb());
        orb.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Chaos Orb");
    }
}
