package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.Preordain;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReturnThePast.class, Shock.class, GrizzlyBears.class, Preordain.class, ThinkTwice.class})
class ReturnThePastTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, instant and sorcery cards in your graveyard have flashback")
    void grantsFlashbackDuringYourTurn() {
        harness.addToBattlefield(player1, new ReturnThePast());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock()));
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Return the Past does not grant flashback during an opponent's turn")
    void grantsFlashbackOnlyDuringYourTurn() {
        harness.addToBattlefield(player1, new ReturnThePast());
        harness.setGraveyard(player1, List.of(new Shock()));
        prepareMainPhase(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Return the Past does not grant flashback to creature cards")
    void doesNotGrantFlashbackToCreatures() {
        harness.addToBattlefield(player1, new ReturnThePast());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A card with printed flashback can use the granted mana-cost flashback")
    void canUseGrantedCostInsteadOfPrintedFlashbackCost() {
        harness.addToBattlefield(player1, new ReturnThePast());
        ThinkTwice spell = new ThinkTwice();
        harness.setGraveyard(player1, List.of(spell));
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
    }

    @Test
    @DisplayName("Sorceries gain flashback for their mana cost during the controller's main phase")
    void grantsFlashbackToSorceries() {
        harness.addToBattlefield(player1, new ReturnThePast());
        Preordain spell = new Preordain();
        harness.setGraveyard(player1, List.of(spell));
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        harness.assertNotInGraveyard(player1, "Preordain");
    }

    @Test
    @DisplayName("Granted flashback does not allow sorceries during upkeep")
    void sorceriesRetainTimingRestrictions() {
        harness.addToBattlefield(player1, new ReturnThePast());
        harness.setGraveyard(player1, List.of(new Preordain()));
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted flashback exiles the spell after resolution")
    void grantedFlashbackExilesResolvedSpell() {
        harness.addToBattlefield(player1, new ReturnThePast());
        Shock spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Return the Past does not grant flashback to an opponent's graveyard")
    void doesNotGrantFlashbackToOpponentsGraveyard() {
        harness.addToBattlefield(player1, new ReturnThePast());
        harness.setGraveyard(player2, List.of(new Shock()));
        prepareMainPhase(player1);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The grant ends when Return the Past leaves the battlefield")
    void grantEndsWhenSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new ReturnThePast());
        harness.setGraveyard(player1, List.of(new Shock()));
        prepareMainPhase(player1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
