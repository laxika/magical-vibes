package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BleedDry;
import com.github.laxika.magicalvibes.cards.s.SelhoffEntomber;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({FellStinger.class, SelhoffEntomber.class, Forest.class, Island.class, BleedDry.class})
class FellStingerTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit does not draw cards or lose life")
    void decliningExploitDoesNothing() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SelhoffEntomber());
        harness.setLibrary(player2, List.of(new Forest(), new Island()));
        harness.setLife(player2, 20);
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castFellStinger();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Fell Stinger");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Exploiting a creature makes a target player draw two cards and lose 2 life")
    void exploitAffectsTargetPlayer() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SelhoffEntomber());
        harness.setLibrary(player2, List.of(new Forest(), new Island()));
        harness.setLife(player2, 20);
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castFellStinger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Selhoff Entomber");
        harness.assertOnBattlefield(player1, "Fell Stinger");
    }

    @Test
    @DisplayName("Exploit can target its controller")
    void exploitCanTargetController() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SelhoffEntomber());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.setLife(player1, 20);

        castFellStinger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Exploit trigger cannot target a permanent")
    void exploitCannotTargetPermanent() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SelhoffEntomber());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SelhoffEntomber());

        castFellStinger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fell Stinger can exploit itself and still give its controller the payoff")
    void exploitingItselfTriggersPayoff() {
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.setLife(player1, 20);

        castFellStinger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Fell Stinger"));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fell Stinger");
        harness.assertInGraveyard(player1, "Fell Stinger");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Exploit cannot sacrifice an opponent's creature")
    void cannotExploitOpponentsCreature() {
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SelhoffEntomber());

        castFellStinger();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Selhoff Entomber");

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Fell Stinger"));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Fell Stinger");
    }

    @Test
    @DisplayName("Removing Fell Stinger before exploit resolves prevents the payoff but permits sacrifice")
    void removalBeforeExploitPreventsPayoff() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SelhoffEntomber());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FellStinger(), "{2}{B}");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new BleedDry()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Fell Stinger"));
        harness.assertNotOnBattlefield(player1, "Fell Stinger");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertInGraveyard(player1, "Selhoff Entomber");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void castFellStinger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FellStinger(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
