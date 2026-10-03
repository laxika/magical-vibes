package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.w.WearDown;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarrotCake.class, LeylineOfTheVoid.class, WearDown.class})
class CarrotCakeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a Rabbit and starts scry 1")
    void enteringCreatesRabbitAndScries() {
        harness.setHand(player1, List.of(new CarrotCake()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(rabbitCount()).isEqualTo(1);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing gains 3 life and creates a Rabbit before scrying 1")
    void sacrificingGainsLifeCreatesRabbitAndScries() {
        harness.addToBattlefield(player1, new CarrotCake());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(rabbitCount()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Carrot Cake");

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Scry can move the top card to the bottom after creating the Rabbit")
    void enteringCanPutTopCardOnBottom() {
        CarrotCake top = new CarrotCake();
        CarrotCake next = new CarrotCake();
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new CarrotCake()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(rabbitCount()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent the sacrifice Rabbit or life gain")
    void sacrificingWithEmptyLibraryStillCreatesRabbitAndGainsLife() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new CarrotCake());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(rabbitCount()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Carrot Cake");
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Sacrifice triggers even when Leyline of the Void exiles the Cake")
    void sacrificeTriggerWorksWhenGraveyardMoveIsReplaced() {
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        CarrotCake cake = new CarrotCake();
        harness.addToBattlefield(player1, cake);
        harness.setLibrary(player1, List.of(new CarrotCake()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(cake.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Carrot Cake");
        assertThat(rabbitCount()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.assertLife(player1, 10);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        assertThat(rabbitCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroying the Cake does not trigger its sacrifice ability")
    void destructionDoesNotCreateRabbitOrScry() {
        var cake = harness.addToBattlefieldAndReturn(player2, new CarrotCake());
        harness.setHand(player1, List.of(new WearDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithGift(player1, 0, List.of(cake.getId()), false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Carrot Cake");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A tapped Cake cannot pay the tap cost")
    void tappedCakeCannotActivate() {
        var cake = harness.addToBattlefieldAndReturn(player1, new CarrotCake());
        cake.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Carrot Cake");
        harness.assertNotInGraveyard(player1, "Carrot Cake");
        assertThat(rabbitCount()).isZero();
    }

    private long rabbitCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.RABBIT))
                .count();
    }
}
