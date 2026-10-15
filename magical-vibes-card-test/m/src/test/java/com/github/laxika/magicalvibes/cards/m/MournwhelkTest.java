package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mournwhelk.class, WoodlandChangeling.class, AxegrinderGiant.class, RayOfCommand.class})
class MournwhelkTest extends BaseCardTest {

    private void castMournwhelk(java.util.UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new Mournwhelk())));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("Hardcast: ETB makes target player discard two cards and Mournwhelk stays on the battlefield")
    void hardcastDiscardsTwoAndStays() {
        harness.setHand(player2, new ArrayList<>(List.of(new WoodlandChangeling(), new AxegrinderGiant())));
        castMournwhelk(player2.getId());

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);

        harness.assertOnBattlefield(player1, "Mournwhelk");
    }

    @Test
    @DisplayName("Can target itself controller (any player): controller discards two cards")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new Mournwhelk(), new WoodlandChangeling(), new AxegrinderGiant())));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, player1.getId());

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Evoke: cast for {3}{B}, ETB still forces the discard, then Mournwhelk is sacrificed")
    void evokeDiscardsThenSacrifices() {
        harness.setHand(player2, new ArrayList<>(List.of(new WoodlandChangeling(), new AxegrinderGiant())));
        harness.setHand(player1, new ArrayList<>(List.of(new Mournwhelk())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithEvoke(player1, 0, player2.getId());
        harness.passBothPriorities();
        chooseEvokeOrder(true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);

        harness.assertOnBattlefield(player1, "Mournwhelk");
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Mournwhelk");
        harness.assertInGraveyard(player1, "Mournwhelk");
    }

    @Test
    @DisplayName("ETB discards only the available card when target has fewer than two cards")
    void discardsWhatIsAvailable() {
        harness.setHand(player2, new ArrayList<>(List.of(new WoodlandChangeling())));
        castMournwhelk(player2.getId());

        resolveAllTriggers();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Woodland Changeling");
    }

    private void chooseEvokeOrder(boolean discardFirst) {
        var order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        assertThat(order.playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, order.options().stream()
                .filter(option -> option.contains("sacrifice") == discardFirst)
                .findFirst().orElseThrow());
    }

    @Test
    @DisplayName("An empty target hand does not prevent the evoke sacrifice")
    void emptyHandStillSacrificesWhenEvoked() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Mournwhelk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreatureWithEvoke(player1, 0, player2.getId());
        harness.passBothPriorities();
        chooseEvokeOrder(true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Mournwhelk");
        harness.assertInGraveyard(player1, "Mournwhelk");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The discard trigger resolves even when the evoke sacrifice resolves first")
    void discardResolvesAfterEvokeSacrifice() {
        harness.setHand(player2, List.of(new WoodlandChangeling(), new AxegrinderGiant(), new Mournwhelk()));
        harness.setHand(player1, List.of(new Mournwhelk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreatureWithEvoke(player1, 0, player2.getId());
        harness.passBothPriorities();
        chooseEvokeOrder(false);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mournwhelk");
        harness.assertInGraveyard(player1, "Mournwhelk");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Axegrinder Giant");
        harness.assertInGraveyard(player2, "Mournwhelk");
        harness.assertInGraveyard(player2, "Woodland Changeling");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The current controller must sacrifice an evoked Mournwhelk after control changes")
    void evokeSacrificesAfterOpponentGainsControl() {
        harness.setHand(player1, List.of(new Mournwhelk()));
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreatureWithEvoke(player1, 0, player2.getId());
        harness.passBothPriorities();
        chooseEvokeOrder(false);

        var mournwhelkId = harness.getPermanentId(player1, "Mournwhelk");
        harness.castAndResolveInstant(player2, 0, mournwhelkId);
        harness.assertOnBattlefield(player2, "Mournwhelk");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Mournwhelk");
        harness.assertNotOnBattlefield(player2, "Mournwhelk");
        harness.assertInGraveyard(player1, "Mournwhelk");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
