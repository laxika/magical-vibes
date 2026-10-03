package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.cards.s.SecretRendezvous;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CogworkArchivist.class, EagerFirstYear.class, SecretRendezvous.class})
class CogworkArchivistTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target card from your graveyard on the bottom of its owner's library")
    void putsOwnGraveyardCardOnLibraryBottom() {
        Card target = new EagerFirstYear();
        Card existingTop = new SecretRendezvous();
        Card existingBottom = new SecretRendezvous();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(existingTop, existingBottom));
        addReadyArchivist();

        activate(target);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingTop, existingBottom, target);
    }

    @Test
    @DisplayName("Puts a target card from an opponent's graveyard on the bottom of its owner's library")
    void putsOpponentGraveyardCardOnOwnerLibraryBottom() {
        Card target = new EagerFirstYear();
        Card existingTop = new SecretRendezvous();
        Card existingBottom = new SecretRendezvous();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(existingTop, existingBottom));
        addReadyArchivist();

        activate(target);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, existingBottom, target);
    }

    @Test
    @DisplayName("Rejects a target that is not a card in a graveyard")
    void rejectsNonGraveyardTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear());
        addReadyArchivist();

        assertThatThrownBy(() -> activate(target.getCard()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPutNoncreatureCardIntoEmptyLibrary() {
        Card target = new SecretRendezvous();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of());
        addReadyArchivist();

        activate(target);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target);
    }

    @Test
    void paysTapCostImmediatelyAndCannotActivateAgainWhileTapped() {
        Card target = new EagerFirstYear();
        harness.setGraveyard(player1, List.of(target));
        Permanent archivist = addReadyArchivist();
        prepareActivation(2);

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);

        assertThat(archivist.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        Card target = new EagerFirstYear();
        harness.setGraveyard(player1, List.of(target));
        Permanent archivist = addReadyArchivist();
        prepareActivation(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(archivist.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Card target = new EagerFirstYear();
        harness.setGraveyard(player1, List.of(target));
        Permanent archivist = addReadyArchivist();
        archivist.setSummoningSick(true);
        prepareActivation(2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(archivist.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotMoveTargetThatLeavesGraveyardBeforeResolution() {
        Card target = new EagerFirstYear();
        Card existing = new SecretRendezvous();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(existing));
        addReadyArchivist();
        prepareActivation(2);
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existing);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterArchivistLeavesBattlefield() {
        Card target = new EagerFirstYear();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of());
        Permanent archivist = addReadyArchivist();
        prepareActivation(2);
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).remove(archivist);
        harness.setGraveyard(player1, List.of(archivist.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(archivist.getCard());
    }

    private Permanent addReadyArchivist() {
        return addCreatureReady(player1, new CogworkArchivist());
    }

    private void activate(Card target) {
        prepareActivation(2);
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
    }

    private void prepareActivation(int mana) {
        harness.addMana(player1, ManaColor.COLORLESS, mana);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
