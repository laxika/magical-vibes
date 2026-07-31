package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WantedScoundrelsTest extends BaseCardTest {

    @Test
    @DisplayName("When Wanted Scoundrels dies, controller is prompted to target an opponent")
    void deathTriggerPromptsForOpponent() {
        harness.addToBattlefield(player2, new WantedScoundrels());
        killScoundrels();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Death trigger creates two Treasure tokens under the targeted opponent's control")
    void deathCreatesTwoTreasuresForOpponent() {
        harness.addToBattlefield(player2, new WantedScoundrels());
        killScoundrels();

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        List<Permanent> opponentTreasures = findPermanents(player1, "Treasure");
        assertThat(opponentTreasures).hasSize(2);
        assertThat(opponentTreasures).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        });
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Wanted Scoundrels goes to the graveyard when it dies")
    void diesToGraveyard() {
        harness.addToBattlefield(player2, new WantedScoundrels());
        killScoundrels();

        harness.assertInGraveyard(player2, "Wanted Scoundrels");
        harness.assertNotOnBattlefield(player2, "Wanted Scoundrels");
    }

    private void killScoundrels() {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID scoundrelsId = harness.getPermanentId(player2, "Wanted Scoundrels");
        harness.castInstant(player1, 0, scoundrelsId);
        harness.passBothPriorities();
    }
}
