package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DuelTactics.class, GrizzlyBears.class})
class DuelTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage and target creature cannot block this turn")
    void dealsDamageAndPreventsBlocking() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DuelTactics()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Flashback exiles Duel Tactics after resolving")
    void flashbackExilesAfterResolving() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new DuelTactics()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
        harness.assertNotInGraveyard(player1, "Duel Tactics");
        GameData gameData = harness.getGameData();
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Duel Tactics"));
    }

    @Test
    @DisplayName("Damage and blocking restriction expire at the end of the turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DuelTactics()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can target your own creature without affecting other creatures")
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DuelTactics()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.getMarkedDamage()).isZero();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        harness.assertInGraveyard(player1, "Duel Tactics");
    }

    @Test
    @DisplayName("Lethal damage destroys the target after the spell resolves")
    void dealsLethalDamage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setMarkedDamage(1);
        harness.setHand(player1, List.of(new DuelTactics()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Duel Tactics");
    }

    @Test
    @DisplayName("Flashback still exiles the spell when its target leaves the battlefield")
    void flashbackExilesWithMissingTarget() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new DuelTactics()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFlashback(player1, 0, target.getId());
        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Duel Tactics");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Duel Tactics"));
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }
}
