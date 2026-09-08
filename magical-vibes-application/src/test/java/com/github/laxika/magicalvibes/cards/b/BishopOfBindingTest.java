package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BishopOfBindingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles target creature an opponent controls")
    void etbExilesOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castBishop(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled creature returns when Bishop of Binding leaves the battlefield")
    void exiledCreatureReturnsWhenBishopLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castBishop(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID bishopId = harness.getPermanentId(player1, "Bishop of Binding");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bishopId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Attacking boosts a target Vampire by the exiled creature's power")
    void attackBoostsVampireByExiledPower() {
        Permanent vampire = addCreatureReady(player1, new AdantoVanguard());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castBishop(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bishop = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Bishop of Binding"))
                .findFirst().orElseThrow();
        bishop.setSummoningSick(false);
        int bishopIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bishop);
        declareAttackers(player1, List.of(bishopIndex));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, vampire.getId());
        harness.passBothPriorities();

        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attack trigger cannot target a non-Vampire creature")
    void attackTriggerCannotTargetNonVampire() {
        addCreatureReady(player1, new AdantoVanguard());
        Permanent nonVampire = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castBishop(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bishop = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Bishop of Binding"))
                .findFirst().orElseThrow();
        bishop.setSummoningSick(false);
        int bishopIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bishop);
        declareAttackers(player1, List.of(bishopIndex));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonVampire.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBishop(UUID targetId) {
        harness.setHand(player1, List.of(new BishopOfBinding()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
