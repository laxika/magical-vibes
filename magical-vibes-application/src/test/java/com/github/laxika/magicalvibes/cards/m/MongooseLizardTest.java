package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MongooseLizard.class, Mountain.class})
class MongooseLizardTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 1 damage to a target player")
    void etbDealsOneDamageToPlayer() {
        harness.setHand(player1, List.of(new MongooseLizard()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Mountaincycling searches for a Mountain and puts it into hand")
    void mountaincyclingSearchesForMountain() {
        Card mountain = new Mountain();
        harness.setHand(player1, List.of(new MongooseLizard()));
        harness.setLibrary(player1, List.of(mountain, new MongooseLizard()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Mongoose Lizard");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(mountain);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(mountain);
        harness.assertInGraveyard(player1, "Mongoose Lizard");
    }

    @Test
    void etbDealsOneDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MongooseLizard());
        harness.setHand(player1, List.of(new MongooseLizard()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void mountaincyclingCanFailToFindAnAvailableMountain() {
        Card mountain = new Mountain();
        harness.setHand(player1, List.of(new MongooseLizard()));
        harness.setLibrary(player1, List.of(mountain));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain);
        harness.assertInGraveyard(player1, "Mongoose Lizard");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mountaincyclingWithNoMountainDoesNotDrawAnotherCard() {
        Card other = new MongooseLizard();
        harness.setHand(player1, List.of(new MongooseLizard()));
        harness.setLibrary(player1, List.of(other));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        harness.assertInGraveyard(player1, "Mongoose Lizard");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mountaincyclingRequiresTwoManaBeforeDiscarding() {
        Card lizard = new MongooseLizard();
        harness.setHand(player1, List.of(lizard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lizard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(lizard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new MongooseLizard());
        addCreatureReady(player2, new MongooseLizard());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new MongooseLizard());
        Permanent first = addCreatureReady(player2, new MongooseLizard());
        Permanent second = addCreatureReady(player2, new MongooseLizard());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
