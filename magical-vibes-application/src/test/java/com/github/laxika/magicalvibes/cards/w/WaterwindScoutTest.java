package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaterwindScout.class, Island.class})
class WaterwindScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Map token")
    void etbCreatesMapToken() {
        createMap();

        assertThat(findPermanents(player1, "Map")).singleElement()
                .satisfies(map -> assertThat(map.getCard().hasType(CardType.ARTIFACT)).isTrue())
                .satisfies(map -> assertThat(map.getCard().getSubtypes()).contains(CardSubtype.MAP));
    }

    @Test
    void mapExploringLandPutsItInHandWithoutCounter() {
        Permanent map = createMap();
        Permanent scout = findPermanent(player1, "Waterwind Scout");
        Island land = new Island();
        harness.setLibrary(player1, List.of(land));

        activateMap(map, scout);
        assertThat(findPermanents(player1, "Map")).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mapExploringNonlandCanKeepItOnTop() {
        Permanent map = createMap();
        Permanent scout = findPermanent(player1, "Waterwind Scout");
        WaterwindScout nonland = new WaterwindScout();
        harness.setLibrary(player1, List.of(nonland));

        activateMap(map, scout);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nonland);
    }

    @Test
    void mapExploringNonlandCanPutItInGraveyard() {
        Permanent map = createMap();
        Permanent scout = findPermanent(player1, "Waterwind Scout");
        WaterwindScout nonland = new WaterwindScout();
        harness.setLibrary(player1, List.of(nonland));

        activateMap(map, scout);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
    }

    @Test
    void mapExploringEmptyLibraryStillAddsCounter() {
        Permanent map = createMap();
        Permanent scout = findPermanent(player1, "Waterwind Scout");
        harness.setLibrary(player1, List.of());

        activateMap(map, scout);
        harness.passBothPriorities();

        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mapCannotTargetOpponentsCreature() {
        Permanent map = createMap();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new WaterwindScout());

        assertThatThrownBy(() -> activateMap(map, opponent)).isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Map")).containsExactly(map);
    }

    @Test
    void mapCannotBeActivatedOutsideMainPhase() {
        Permanent map = createMap();
        Permanent scout = findPermanent(player1, "Waterwind Scout");
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> activateMap(map, scout)).isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Map")).containsExactly(map);
    }

    @Test
    void tappedMapCannotBeActivated() {
        Permanent map = createMap();
        Permanent scout = findPermanent(player1, "Waterwind Scout");
        map.setTapped(true);

        assertThatThrownBy(() -> activateMap(map, scout)).isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Map")).containsExactly(map);
    }

    @Test
    void mapRequiresOneManaToActivate() {
        Permanent map = createMap();
        Permanent scout = findPermanent(player1, "Waterwind Scout");

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(map), 0, null, scout.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Map")).containsExactly(map);
        assertThat(map.isTapped()).isFalse();
    }

    private Permanent createMap() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WaterwindScout(), "{2}{U}");
        resolveAllTriggers();
        return findPermanent(player1, "Map");
    }

    private void activateMap(Permanent map, Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(map),
                0, null, target.getId());
    }
}
