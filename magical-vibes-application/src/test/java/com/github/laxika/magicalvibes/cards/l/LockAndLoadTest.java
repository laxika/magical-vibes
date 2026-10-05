package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LockAndLoad.class, GrizzlyBears.class, Shock.class, Twincast.class})
class LockAndLoadTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card plus one for each other instant or sorcery cast this turn")
    void drawsForOtherInstantAndSorcerySpells() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock(), new Shock(), new LockAndLoad()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Does not count creature spells")
    void doesNotCountCreatureSpells() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new LockAndLoad()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void countsAnotherLockAndLoadCastThisTurn() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new LockAndLoad(), new LockAndLoad()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void copyCountsTheOriginalAndTheCopyingSpell() {
        LockAndLoad spell = new LockAndLoad();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(spell, new Twincast()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.castAndResolveInstant(player1, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
    }

    @Test
    void opponentsCopyCountsOnlyThatOpponentsCastSpells() {
        LockAndLoad spell = new LockAndLoad();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.castFromHand(player1, spell, "{2}{U}");
        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotPlotWithoutFullCostOrOutsideMainPhase() {
        LockAndLoad spell = new LockAndLoad();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void plottingPaysFourManaAndAllowsFreeCastOnlyOnALaterTurn() {
        LockAndLoad spell = new LockAndLoad();
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
