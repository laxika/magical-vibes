package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuggedHighlands;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagmaticHellkite.class, Forest.class, RuggedHighlands.class})
class MagmaticHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an opponent's nonbasic land and fetches a tapped basic land with a stun counter")
    void destroysNonbasicLandAndFetchesStunnedBasicLand() {
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new RuggedHighlands());
        harness.setLibrary(player2, List.of(new Forest(), new MagmaticHellkite()));
        prepareCast();

        harness.castCreature(player1, 0, List.of(nonbasicLand.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rugged Highlands");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        Permanent fetched = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest"))
                .findFirst()
                .orElseThrow();
        assertThat(fetched.isTapped()).isTrue();
        assertThat(fetched.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        Permanent basicLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(basicLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an own nonbasic land")
    void cannotTargetOwnNonbasicLand() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new RuggedHighlands());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can enter when there is no legal target for the triggered ability")
    void entersWithoutLegalTarget() {
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new MagmaticHellkite(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Magmatic Hellkite");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's nonland permanent")
    void cannotTargetNonland() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MagmaticHellkite());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not search if the target leaves before the trigger resolves")
    void doesNotSearchForMissingTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RuggedHighlands());
        harness.setLibrary(player2, List.of(new Forest()));
        prepareCast();
        harness.castCreature(player1, 0, List.of(land.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerGraveyards.get(player2.getId()).add(land.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Magmatic Hellkite");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Still searches when the target land is indestructible")
    void searchesDespiteIndestructibleLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RuggedHighlands());
        land.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setLibrary(player2, List.of(new Forest()));
        prepareCast();
        harness.castCreature(player1, 0, List.of(land.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Rugged Highlands");
        harness.assertNotInGraveyard(player2, "Rugged Highlands");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player2, 0);

        Permanent forest = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest"))
                .findFirst().orElseThrow();
        assertThat(forest.isTapped()).isTrue();
        assertThat(forest.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The land's controller can fail to find even when a basic land is available")
    void canFailToFindBasicLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RuggedHighlands());
        harness.setLibrary(player2, List.of(new Forest()));
        prepareCast();
        harness.castCreature(player1, 0, List.of(land.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, -1);

        harness.assertInGraveyard(player2, "Rugged Highlands");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Destroys the land even when its controller has no basic land to find")
    void destroysLandWithoutMatchingBasicLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RuggedHighlands());
        harness.setLibrary(player2, List.of(new RuggedHighlands(), new MagmaticHellkite()));
        prepareCast();
        harness.castCreature(player1, 0, List.of(land.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rugged Highlands");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The fetched land skips its first untap and untaps on the following turn")
    void stunCounterReplacesFirstUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RuggedHighlands());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new MagmaticHellkite(), new MagmaticHellkite()));
        prepareCast();
        harness.castCreature(player1, 0, List.of(land.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        Permanent forest = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest"))
                .findFirst().orElseThrow();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(forest.isTapped()).isTrue();
        assertThat(forest.getCounterCount(CounterType.STUN)).isZero();

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(forest.isTapped()).isFalse();
        assertThat(forest.getCounterCount(CounterType.STUN)).isZero();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new MagmaticHellkite()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
