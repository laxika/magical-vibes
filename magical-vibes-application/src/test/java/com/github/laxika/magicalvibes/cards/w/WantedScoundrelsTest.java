package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RiversRebuke;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WantedScoundrels.class, WalkThePlank.class, RiversRebuke.class})
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

    @Test
    @DisplayName("Death trigger gives Treasures to the opponent when its controller destroys it")
    void controllerDestroyingScoundrelsGivesTreasuresToOpponent() {
        harness.addToBattlefield(player1, new WantedScoundrels());
        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Wanted Scoundrels"));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Returning Wanted Scoundrels to hand does not create Treasures")
    void returningToHandDoesNotTriggerDeathAbility() {
        harness.addToBattlefield(player2, new WantedScoundrels());
        harness.setHand(player1, List.of(new RiversRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInHand(player2, "Wanted Scoundrels");
        harness.assertNotOnBattlefield(player2, "Wanted Scoundrels");
        harness.assertNotInGraveyard(player2, "Wanted Scoundrels");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The opponent can immediately sacrifice a new Treasure for any color of mana")
    void treasureProducesManaImmediately(ManaColor color) {
        harness.addToBattlefield(player2, new WantedScoundrels());
        killScoundrels();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, index, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void killScoundrels() {
        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID scoundrelsId = harness.getPermanentId(player2, "Wanted Scoundrels");
        harness.castAndResolveSorcery(player1, 0, scoundrelsId);
    }
}
