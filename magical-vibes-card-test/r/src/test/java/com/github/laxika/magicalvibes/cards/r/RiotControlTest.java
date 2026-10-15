package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiotControl.class, GrizzlyBears.class, Shock.class})
class RiotControlTest extends BaseCardTest {

    private void castRiotControl() {
        harness.castFromHand(player2, new RiotControl(), "{2}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gains 1 life for each creature opponents control")
    void gainsLifePerOpponentCreature() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castRiotControl();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Gains no life when opponents control no creatures")
    void gainsNoLifeWithoutOpponentCreatures() {
        harness.setLife(player2, 20);

        castRiotControl();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents damage that would be dealt to the controller")
    void preventsDamageToController() {
        harness.setLife(player2, 20);

        castRiotControl();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage dealt to creatures the controller controls")
    void doesNotPreventDamageToControlledCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRiotControl();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The prevention wears off at end of turn")
    void preventionWearsOff() {
        harness.setLife(player2, 20);

        castRiotControl();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents combat damage to the controller")
    void preventsCombatDamage() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        castRiotControl();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Counts opponent creatures when Riot Control resolves")
    void countsCreaturesAtResolution() {
        harness.setLife(player2, 20);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player2, new RiotControl(), "{2}{W}");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevention applies to every damage event this turn")
    void preventsRepeatedDamage() {
        harness.setLife(player2, 20);
        castRiotControl();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not prevent damage to the opponent")
    void doesNotPreventDamageToOpponent() {
        harness.setLife(player1, 20);
        castRiotControl();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }
}
