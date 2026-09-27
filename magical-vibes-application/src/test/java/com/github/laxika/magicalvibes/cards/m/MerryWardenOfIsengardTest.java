package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerryWardenOfIsengard.class, Ornithopter.class})
class MerryWardenOfIsengardTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Pippin")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card pippin = namedCard("Pippin, Warden of Isengard");
        harness.setLibrary(player2, List.of(pippin));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new MerryWardenOfIsengard());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(pippin);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An artifact entering creates a lifelink Soldier")
    void artifactEntryCreatesLifelinkSoldier() {
        harness.addToBattlefield(player1, new MerryWardenOfIsengard());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().getKeywords()).contains(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("The artifact trigger fires only once each turn")
    void artifactTriggerFiresOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new MerryWardenOfIsengard());
        harness.setHand(player1, List.of(new Ornithopter(), new Ornithopter()));

        castArtifactAndResolveTrigger();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control does not trigger")
    void opponentArtifactEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new MerryWardenOfIsengard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Ornithopter()));

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    private void castArtifactAndResolveTrigger() {
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
