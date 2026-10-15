package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.l.LurkingLizards;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnstableExperiment.class, LurkingLizards.class, Mountain.class})
class UnstableExperimentTest extends BaseCardTest {

    @Test
    void targetPlayerDrawsThenControlledCreatureConnives() {
        Permanent creature = addCreatureReady(player1, new LurkingLizards());
        harness.setHand(player1, List.of(new UnstableExperiment(), new Mountain()));
        harness.setLibrary(player1, List.of(new LurkingLizards()));
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), creature.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Lurking Lizards");

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).contains("Mountain");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName).contains("Mountain");
    }

    @Test
    void mayOmitCreatureTarget() {
        harness.setHand(player1, List.of(new UnstableExperiment(), new Mountain()));
        harness.setLibrary(player1, List.of(new LurkingLizards()));
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .contains("Mountain")
                .doesNotContain("Lurking Lizards");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName).contains("Mountain");
    }

    @Test
    void discardingLandDoesNotPutCounterOnCreature() {
        Permanent creature = addCreatureReady(player1, new LurkingLizards());
        harness.setHand(player1, List.of(new UnstableExperiment(), new LurkingLizards()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), creature.getId()));
        discardByName("Mountain");

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInHand(player1, "Lurking Lizards");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInHand(player2, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void targetingYourselfDrawsBeforeConniving() {
        Permanent creature = addCreatureReady(player1, new LurkingLizards());
        harness.setHand(player1, List.of(new UnstableExperiment()));
        harness.setLibrary(player1, List.of(new Mountain(), new LurkingLizards()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId(), creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Mountain", "Lurking Lizards");
        discardByName("Lurking Lizards");

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void removedCreatureTargetDoesNotConniveButPlayerStillDraws() {
        Permanent creature = addCreatureReady(player1, new LurkingLizards());
        harness.setHand(player1, List.of(new UnstableExperiment(), new Mountain()));
        harness.setLibrary(player1, List.of(new LurkingLizards()));
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(player2.getId(), creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        harness.assertInHand(player2, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotTargetOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new LurkingLizards());
        harness.setHand(player1, List.of(new UnstableExperiment()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(player2.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void playerTargetIsRequired() {
        harness.setHand(player1, List.of(new UnstableExperiment()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.<java.util.UUID>of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
