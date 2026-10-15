package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiptideDirector.class, FugitiveWizard.class, RidgetopRaptor.class})
class RiptideDirectorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card for each Wizard controlled and ignores the opponent's Wizards")
    void drawsForEachControlledWizard() {
        harness.setHand(player1, List.of());
        Permanent director = addCreatureReady(player1, new RiptideDirector());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new RidgetopRaptor());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setLibrary(player1, List.of(new RidgetopRaptor(), new RidgetopRaptor(), new RidgetopRaptor()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, director), null, null);
        harness.passBothPriorities();

        assertThat(director.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void countsWizardsEnteringBeforeResolution() {
        harness.setHand(player1, List.of());
        Permanent director = addCreatureReady(player1, new RiptideDirector());
        harness.setLibrary(player1, List.of(new RidgetopRaptor(), new RidgetopRaptor()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, battlefieldIndex(player1, director), null, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.enterBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void countsOnlyRemainingWizardsAfterDirectorLeaves(boolean anotherWizardRemains) {
        harness.setHand(player1, List.of());
        Permanent director = addCreatureReady(player1, new RiptideDirector());
        if (anotherWizardRemains) {
            harness.addToBattlefield(player1, new FugitiveWizard());
        }
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setLibrary(player1, List.of(new RidgetopRaptor(), new RidgetopRaptor()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, battlefieldIndex(player1, director), null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, director));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Riptide Director");
        int expectedDraws = anotherWizardRemains ? 1 : 0;
        assertThat(gd.playerHands.get(player1.getId())).hasSize(expectedDraws);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2 - expectedDraws);
    }

    @Test
    void cannotActivateWithOnlyOneBlueMana() {
        Permanent director = addCreatureReady(player1, new RiptideDirector());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(player1, director), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(director.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent director = harness.addToBattlefieldAndReturn(player1, new RiptideDirector());
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(player1, director), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(director.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent director = addCreatureReady(player1, new RiptideDirector());
        director.tap();
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(player1, director), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
