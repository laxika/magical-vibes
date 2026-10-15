package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BoneShards;
import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticRedaction.class, Censor.class, GrizzlyBears.class,
        BoneShards.class, OrnithopterOfParadise.class})
class MysticRedactionTest extends BaseCardTest {

    @Test
    @DisplayName("Scries 1 at the beginning of its controller's upkeep")
    void scriesAtBeginningOfUpkeep() {
        Card top = new GrizzlyBears();
        Card bottom = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.addToBattlefield(player1, new MysticRedaction());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
    }

    @Test
    @DisplayName("Each opponent mills two cards when its controller discards")
    void eachOpponentMillsTwoOnControllerDiscard() {
        harness.addToBattlefield(player1, new MysticRedaction());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void canKeepScriedCardOnTop() {
        Card top = new MysticRedaction();
        Card bottom = new MysticRedaction();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.addToBattlefield(player1, new MysticRedaction());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
    }

    @Test
    void doesNotScryDuringOpponentsUpkeep() {
        Card top = new MysticRedaction();
        harness.setLibrary(player1, List.of(top));
        harness.addToBattlefield(player1, new MysticRedaction());
        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void ordinaryDiscardCostMillsOnlyOpponentAndUsesTopCards() {
        harness.addToBattlefield(player1, new MysticRedaction());
        var target = harness.addToBattlefieldAndReturn(player1, new OrnithopterOfParadise());
        Card first = new MysticRedaction();
        Card second = new MysticRedaction();
        Card third = new MysticRedaction();
        Card ownTop = new MysticRedaction();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setHand(player1, List.of(new BoneShards(), new MysticRedaction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
    }

    @Test
    void opponentDiscardDoesNotTriggerMill() {
        harness.addToBattlefield(player1, new MysticRedaction());
        var target = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());
        Card ownTop = new MysticRedaction();
        Card opponentTop = new MysticRedaction();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setHand(player2, List.of(new BoneShards(), new MysticRedaction()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.castInstantWithDiscard(player2, 0, target.getId(), 1);
        resolveAllTriggers();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
    }

    @Test
    void millsRemainingCardWhenOpponentHasOnlyOneCard() {
        harness.addToBattlefield(player1, new MysticRedaction());
        var target = harness.addToBattlefieldAndReturn(player1, new OrnithopterOfParadise());
        Card last = new MysticRedaction();
        harness.setLibrary(player2, List.of(last));
        harness.setHand(player1, List.of(new BoneShards(), new MysticRedaction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        resolveAllTriggers();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(last);
    }

}
