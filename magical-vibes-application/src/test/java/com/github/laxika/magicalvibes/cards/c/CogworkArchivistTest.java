package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CogworkArchivistTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target card from your graveyard on the bottom of its owner's library")
    void putsOwnGraveyardCardOnLibraryBottom() {
        Card target = new GrizzlyBears();
        Card existingTop = new Shock();
        Card existingBottom = new Shock();
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
        Card target = new GrizzlyBears();
        Card existingTop = new Shock();
        Card existingBottom = new Shock();
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addReadyArchivist();

        assertThatThrownBy(() -> activate(target.getCard()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyArchivist() {
        Permanent archivist = new Permanent(new CogworkArchivist());
        archivist.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(archivist);
        return archivist;
    }

    private void activate(Card target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
    }
}
