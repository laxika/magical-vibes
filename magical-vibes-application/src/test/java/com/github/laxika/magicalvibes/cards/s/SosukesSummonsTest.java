package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PetalmaneBaku;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SosukesSummons.class, SakuraTribeSpringcaller.class, PetalmaneBaku.class})
class SosukesSummonsTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting creates two 1/1 green Snake creature tokens")
    void createsTwoSnakeTokens() {
        prepareMain(player1);
        harness.castFromHand(player1, new SosukesSummons(), "{2}{G}");

        harness.passBothPriorities();

        List<Permanent> snakes = findPermanents(player1, "Snake");
        assertThat(snakes).hasSize(2);
        assertThat(snakes).allSatisfy(snake -> {
            assertThat(snake.getCard().isToken()).isTrue();
            assertThat(snake.getCard().getSubtypes()).containsExactly(CardSubtype.SNAKE);
            assertThat(snake.getEffectivePower()).isEqualTo(1);
            assertThat(snake.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Created Snake tokens are green")
    void createsGreenSnakeTokens() {
        prepareMain(player1);
        harness.castFromHand(player1, new SosukesSummons(), "{2}{G}");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake"))
                .hasSize(2)
                .allSatisfy(snake -> assertThat(snake.getCard().getColor()).isEqualTo(CardColor.GREEN));
    }

    @Test
    @DisplayName("A nontoken Snake entering lets you return Sosuke's Summons from your graveyard")
    void nontokenSnakeReturnsSummons() {
        SosukesSummons summons = new SosukesSummons();
        harness.setGraveyard(player1, List.of(summons));
        prepareMain(player1);

        harness.castFromHand(player1, new SakuraTribeSpringcaller(), "{3}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(summons.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(summons.getId()));
    }

    @Test
    @DisplayName("Declining the optional return leaves Sosuke's Summons in the graveyard")
    void decliningReturnLeavesSummonsInGraveyard() {
        SosukesSummons summons = new SosukesSummons();
        harness.setGraveyard(player1, List.of(summons));
        prepareMain(player1);

        harness.castFromHand(player1, new SakuraTribeSpringcaller(), "{3}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(summons.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(summons.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A nontoken Snake an opponent controls does not trigger the graveyard ability")
    void opponentSnakeDoesNotTrigger() {
        SosukesSummons summons = new SosukesSummons();
        harness.setGraveyard(player1, List.of(summons));

        harness.enterBattlefieldAndReturn(player2, new SakuraTribeSpringcaller());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(summons.getId()));
    }

    @Test
    @DisplayName("Snake tokens do not trigger the graveyard ability")
    void snakeTokensDoNotTrigger() {
        SosukesSummons summonsInGraveyard = new SosukesSummons();
        harness.setGraveyard(player1, List.of(summonsInGraveyard));
        prepareMain(player1);

        harness.castFromHand(player1, new SosukesSummons(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(summonsInGraveyard.getId()));
    }

    @Test
    @DisplayName("A nontoken creature that is not a Snake does not trigger the return")
    void nonSnakeDoesNotTrigger() {
        SosukesSummons summons = new SosukesSummons();
        harness.setGraveyard(player1, List.of(summons));

        harness.enterBattlefieldAndReturn(player1, new PetalmaneBaku());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(summons);
    }

    @Test
    @DisplayName("Sosuke's Summons in hand does not trigger when a Snake enters")
    void summonsInHandDoesNotTrigger() {
        SosukesSummons summons = new SosukesSummons();
        harness.setHand(player1, List.of(summons));

        harness.enterBattlefieldAndReturn(player1, new SakuraTribeSpringcaller());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(summons);
    }

    @Test
    @DisplayName("Each graveyard copy triggers independently and returns only itself")
    void graveyardCopiesReturnIndependently() {
        SosukesSummons first = new SosukesSummons();
        SosukesSummons second = new SosukesSummons();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(first, second));
        prepareMain(player1);

        harness.enterBattlefieldAndReturn(player1, new SakuraTribeSpringcaller());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .doesNotContainAnyElementsOf(gd.playerGraveyards.get(player1.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A return trigger cannot retrieve its source from exile")
    void sourceRemovedFromGraveyardIsNotReturned() {
        SosukesSummons summons = new SosukesSummons();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(summons));
        prepareMain(player1);

        harness.enterBattlefieldAndReturn(player1, new SakuraTribeSpringcaller());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(summons));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(summons);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(summons);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An old return trigger cannot retrieve a new graveyard incarnation")
    void sourceLeavingAndReenteringGraveyardIsNotReturned() {
        SosukesSummons summons = new SosukesSummons();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(summons));
        gd.markGraveyardEntry(summons);
        prepareMain(player1);

        harness.enterBattlefieldAndReturn(player1, new SakuraTribeSpringcaller());

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(summons));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(summons));
        gd.markGraveyardEntry(summons);

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(summons);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(summons);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
