package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SanguinaryPriest;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TombFortress.class, Forest.class, SanguinaryPriest.class})
class TombFortressTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for black mana")
    void entersTappedAndTapsForBlackMana() {
        harness.setHand(player1, List.of(new TombFortress()));
        harness.playLand(player1, 0);
        Permanent fortress = findPermanent(player1, "Tomb Fortress");

        assertThat(fortress.isTapped()).isTrue();
        fortress.untap();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles itself, mills four, and returns a creature from the graveyard")
    void exilesMillsAndReturnsCreature() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        SanguinaryPriest creature = new SanguinaryPriest();
        Permanent fortress = harness.addToBattlefieldAndReturn(player1, new TombFortress());
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(fortress.getCard().getId()));
    }

    @Test
    @DisplayName("A creature milled by the ability can be returned")
    void returnsNewlyMilledCreature() {
        SanguinaryPriest creature = new SanguinaryPriest();
        Forest land = new Forest();
        harness.addToBattlefield(player1, new TombFortress());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(land, creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Tomb Fortress");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, creature);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        harness.assertOnBattlefield(player1, "Sanguinary Priest");
        assertThat(findPermanent(player1, "Sanguinary Priest").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Returning an eligible creature cannot be declined")
    void cannotDeclineCreatureReturn() {
        harness.addToBattlefield(player1, new TombFortress());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new SanguinaryPriest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Sanguinary Priest");
    }

    @Test
    @DisplayName("Ability mills without an eligible creature and ignores opposing graveyards")
    void resolvesWithoutCreatureInControllersGraveyard() {
        Forest land = new Forest();
        SanguinaryPriest opponentsCreature = new SanguinaryPriest();
        harness.addToBattlefield(player1, new TombFortress());
        harness.setLibrary(player1, List.of(land));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCreature);
        harness.assertNotOnBattlefield(player1, "Sanguinary Priest");
        harness.assertNotOnBattlefield(player1, "Tomb Fortress");
    }

    @Test
    @DisplayName("Reanimation cannot be activated outside a main phase")
    void cannotActivateDuringCombat() {
        Permanent fortress = harness.addToBattlefieldAndReturn(player1, new TombFortress());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Tomb Fortress");
        assertThat(fortress.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Tomb Fortress cannot pay the reanimation tap cost")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new TombFortress()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Tomb Fortress");
        assertThat(gd.stack).isEmpty();
    }
}
