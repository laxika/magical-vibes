package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SummonUndead.class, Forest.class, GrizzlyBears.class})
class SummonUndeadTest extends BaseCardTest {

    @Test
    void acceptingMayMillsThenReturnsCreatureFromGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castSummonUndead();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void decliningMayStillReturnsCreatureWithoutMilling() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castSummonUndead();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotDeclineReturningAnAvailableCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castSummonUndead();
        harness.handleMayAbilityChosen(player1, false);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void canReturnCreatureMilledDuringResolutionWithInitiallyEmptyGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), creature, new Forest(), new Forest()));
        castSummonUndead();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void millsAllAvailableCardsWhenLibraryHasFewerThanThree() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        castSummonUndead();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void noCreatureInOwnGraveyardDoesNotReturnOpponentsCreature() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castSummonUndead();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Summon Undead");
    }

    @Test
    void decliningMillWithEmptyGraveyardFinishesWithoutReturningAnything() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castSummonUndead();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Summon Undead");
    }

    private void castSummonUndead() {
        harness.castFromHand(player1, new SummonUndead(), "{4}{B}");
        harness.passBothPriorities();
    }
}
