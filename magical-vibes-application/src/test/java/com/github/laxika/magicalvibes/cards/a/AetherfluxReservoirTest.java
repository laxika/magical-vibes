package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.c.ConsulateSkygate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherfluxReservoir.class, Shock.class, ConsulateSkygate.class})
class AetherfluxReservoirTest extends BaseCardTest {

    @Test
    @DisplayName("The spell-cast triggers count spells when they resolve")
    void spellCastTriggersCountSpellsAtResolution() {
        harness.addToBattlefield(player1, new AetherfluxReservoir());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Paying 50 life deals 50 damage to a player")
    void paysLifeToDealDamage() {
        harness.addToBattlefield(player1, new AetherfluxReservoir());
        harness.setLife(player1, 60);
        harness.setLife(player2, 60);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }

    @Test
    void countsItsOwnCastAfterEnteringButDoesNotTriggerForItself() {
        harness.setHand(player1, List.of(new AetherfluxReservoir(), new ConsulateSkygate()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Consulate Skygate");
    }

    @Test
    void opponentsSpellsNeitherTriggerNorIncreaseLifeGain() {
        harness.addToBattlefield(player1, new AetherfluxReservoir());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player2.getId());
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.passBothPriorities();
    }

    @Test
    void resolvingTriggersSeparatelyGainsOneThenTwoLife() {
        harness.addToBattlefield(player1, new AetherfluxReservoir());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.passBothPriorities();
    }

    @Test
    void paysLifeImmediatelyAndCanDamageACreature() {
        harness.addToBattlefield(player1, new AetherfluxReservoir());
        var target = harness.addToBattlefieldAndReturn(player2, new ConsulateSkygate());
        harness.setLife(player1, 60);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertLife(player1, 10);
        harness.assertOnBattlefield(player2, "Consulate Skygate");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Consulate Skygate");
        harness.assertNotOnBattlefield(player2, "Consulate Skygate");
        harness.assertLife(player1, 10);
    }

    @Test
    void cannotActivateWithLessThanFiftyLife() {
        harness.addToBattlefield(player1, new AetherfluxReservoir());
        harness.setLife(player1, 49);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        harness.assertLife(player1, 49);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateTwiceWithoutTappingOrPayingMana() {
        harness.addToBattlefield(player1, new AetherfluxReservoir());
        harness.setLife(player1, 110);
        harness.setLife(player2, 110);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 110);

        harness.passBothPriorities();
        harness.assertLife(player2, 60);
        harness.passBothPriorities();
        harness.assertLife(player2, 10);
        harness.assertLife(player1, 10);
    }
}
