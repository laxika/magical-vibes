package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearOfChange.class, GrizzlyBears.class})
class FearOfChangeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles another creature and conjures a creature with mana value four")
    void etbExilesAnotherCreatureAndConjuresByManaValue() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        castFearOfChange();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bear.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> !permanent.getCard().getName().equals("Fear of Change")
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getManaValue() == 4);
    }

    @Test
    @DisplayName("Death trigger exiles another creature and conjures a matching random creature")
    void deathTriggerExilesAnotherCreatureAndConjuresByManaValue() {
        Permanent fear = addCreatureReady(player1, new FearOfChange());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, fear));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fear.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bear.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getManaValue() == 4);
    }

    @Test
    @DisplayName("Controller chooses which other creature to exile")
    void choosesOtherCreature() {
        Permanent kept = addCreatureReady(player1, new GrizzlyBears());
        Permanent exiled = addCreatureReady(player1, new GrizzlyBears());

        castFearOfChange();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(kept.getId(), exiled.getId());
        harness.handlePermanentChosen(player1, exiled.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kept).doesNotContain(exiled);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(exiled.getCard().getId()));
    }

    @Test
    @DisplayName("ETB cannot exile itself or an opponent's creature")
    void etbWithNoOtherControlledCreatureDoesNothing() {
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        castFearOfChange();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Fear of Change");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposing);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("conjures ")).isFalse();
    }

    @Test
    @DisplayName("Death without another controlled creature does not conjure")
    void deathWithNoOtherControlledCreatureDoesNothing() {
        Permanent fear = addCreatureReady(player1, new FearOfChange());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, fear));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fear of Change");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposing);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("conjures ")).isFalse();
    }

    @Test
    @DisplayName("Exile choice excludes the source and opposing creatures")
    void choiceContainsOnlyOtherControlledCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        castFearOfChange();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposing);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(first.getCard().getId()));
    }
    private void castFearOfChange() {
        harness.castFromHand(player1, new FearOfChange(), "{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
