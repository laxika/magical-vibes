package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GalactusDevourerOfWorlds;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverSurferGalactussHerald.class, GalactusDevourerOfWorlds.class, GrizzlyBears.class})
class SilverSurferGalactussHeraldTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield may search for Galactus")
    void enteringMaySearchForGalactus() {
        Card galactus = new GalactusDevourerOfWorlds();
        harness.setLibrary(player1, List.of(galactus));
        harness.castFromHand(player1, new SilverSurferGalactussHerald(), "{5}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(galactus);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Galactus, Devourer of Worlds");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage forces a target creature to attack the damaged player, not their planeswalker")
    void combatDamageForcesTargetToAttackDamagedPlayer() {
        Permanent silverSurfer = addCreatureReady(player1, new SilverSurferGalactussHerald());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player2);
        silverSurfer.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        beginAttackers(player1);
        int targetIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(targetIndex),
                Map.of(targetIndex, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too few attack requirements");

    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player) {
        Card card = new Card() {
        };
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private void beginAttackers(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

}
