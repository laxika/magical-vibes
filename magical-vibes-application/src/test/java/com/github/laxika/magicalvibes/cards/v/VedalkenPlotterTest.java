package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VedalkenPlotter.class, Forest.class, Island.class})
class VedalkenPlotterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exchanges control of the two target lands")
    void exchangesControlOfLands() {
        harness.setHand(player1, List.of(new VedalkenPlotter()));
        addMana();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.castCreature(player1, 0, List.of(own.getId(), opponent.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("ETB fizzles when a target land leaves before resolution")
    void fizzlesWhenTargetGone() {
        harness.setHand(player1, List.of(new VedalkenPlotter()));
        addMana();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.castCreature(player1, 0, List.of(own.getId(), opponent.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target your own land as the opponent's land")
    void cannotTargetOwnLandAsOpponentTarget() {
        harness.setHand(player1, List.of(new VedalkenPlotter()));
        addMana();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent alsoOwn = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(own.getId(), alsoOwn.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land an opponent controls");
    }

    @Test
    @DisplayName("Cannot target an opponent's land as the land you control")
    void cannotTargetOpponentsLandAsOwnTarget() {
        harness.setHand(player1, List.of(new VedalkenPlotter()));
        addMana();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponent.getId(), own.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("First target must be a land you control");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent as the opponent's land")
    void cannotTargetNonlandAsOpponentTarget() {
        harness.setHand(player1, List.of(new VedalkenPlotter()));
        addMana();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent nonLand = harness.addToBattlefieldAndReturn(player2, new VedalkenPlotter());

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(own.getId(), nonLand.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Second target must be a land an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent as the land you control")
    void cannotTargetNonlandAsOwnTarget() {
        harness.setHand(player1, List.of(new VedalkenPlotter()));
        addMana();
        Permanent nonLand = harness.addToBattlefieldAndReturn(player1, new VedalkenPlotter());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(nonLand.getId(), opponent.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("First target must be a land you control");
    }

    @Test
    @DisplayName("No exchange when your target land leaves before resolution")
    void noExchangeWhenOwnLandLeaves() {
        harness.setHand(player1, List.of(new VedalkenPlotter()));
        addMana();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.castCreature(player1, 0, List.of(own.getId(), opponent.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(own);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Exchange survives Plotter leaving and preserves the lands' tapped status")
    void exchangeSurvivesSourceLeaving() {
        harness.setHand(player1, List.of(new VedalkenPlotter()));
        addMana();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Island());
        own.tap();

        harness.castCreature(player1, 0, List.of(own.getId(), opponent.getId()));
        harness.passBothPriorities();
        Permanent plotter = findPermanent(player1, "Vedalken Plotter");
        gd.playerBattlefields.get(player1.getId()).remove(plotter);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(own).doesNotContain(opponent);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponent).doesNotContain(own);
        assertThat(own.isTapped()).isTrue();
        assertThat(opponent.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entering without being cast still chooses and exchanges two lands")
    void exchangesLandsWhenNotCast() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.enterBattlefieldAndReturn(player1, new VedalkenPlotter());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(own).doesNotContain(opponent);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponent).doesNotContain(own);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
