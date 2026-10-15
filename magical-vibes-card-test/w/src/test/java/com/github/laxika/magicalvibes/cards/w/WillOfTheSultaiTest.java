package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TasigurTheGoldenFang;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillOfTheSultai.class, TasigurTheGoldenFang.class, Forest.class, Island.class, Mountain.class,
        GrizzlyBears.class})
class WillOfTheSultaiTest extends BaseCardTest {

    @Test
    void millsTargetPlayerAndReturnsAllLandsTapped() {
        harness.setLibrary(player2, List.of(new Forest(), new Island(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new Forest(), new Island(), new GrizzlyBears()));

        castSingleMode(0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .filteredOn(Permanent::isTapped)
                .hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void putsCountersEqualToLandsAndGrantsTrample() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSingleMode(1, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void commanderAllowsBothModes() {
        TasigurTheGoldenFang commander = new TasigurTheGoldenFang();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player2, List.of(new Forest(), new Island(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new Mountain(), new Island()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WillOfTheSultai()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), target.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .filteredOn(Permanent::isTapped)
                .hasSize(2);
    }

    @Test
    void cannotChooseBothModesWithoutACommander() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WillOfTheSultai()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(player2.getId(), target.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void selfMillReturnsNewlyMilledLandsAsWellAsExistingLands() {
        Forest existingLand = new Forest();
        Forest milledForest = new Forest();
        Island milledIsland = new Island();
        GrizzlyBears milledCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(existingLand));
        harness.setLibrary(player1, List.of(milledForest, milledCreature, milledIsland));

        castSingleMode(0, player1.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(existingLand, milledForest, milledIsland);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCreature)
                .doesNotContain(existingLand, milledForest, milledIsland);
    }

    @Test
    void emptyTargetLibraryDoesNotPreventReturningLands() {
        Forest land = new Forest();
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player1, List.of(land));

        castSingleMode(0, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
    }

    @Test
    void zeroLandsStillGrantsTrampleAndItExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castSingleMode(1, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void countsLandsAtResolutionRatherThanAtCasting() {
        harness.addToBattlefield(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WillOfTheSultai()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(target.getId()), null);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Mountain());

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void commanderInCommandZoneDoesNotAllowBothModes() {
        TasigurTheGoldenFang commander = new TasigurTheGoldenFang();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WillOfTheSultai()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(player2.getId(), target.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesRemainChosenWhenCommanderLeavesBeforeResolution() {
        TasigurTheGoldenFang commander = new TasigurTheGoldenFang();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        Permanent commanderPermanent = harness.addToBattlefieldAndReturn(player1, commander);
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player2, List.of(new Forest(), new Island(), new Mountain()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WillOfTheSultai()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), target.getId()), null);
        gd.playerBattlefields.get(player1.getId()).remove(commanderPermanent);
        gd.playerCommandZones.get(player1.getId()).add(commander);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void firstModeStillResolvesWhenCreatureTargetLeavesBattlefield() {
        TasigurTheGoldenFang commander = new TasigurTheGoldenFang();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        harness.addToBattlefield(player1, commander);
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player2, List.of(new Forest(), new Island(), new Mountain()));
        GrizzlyBears creature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setHand(player1, List.of(new WillOfTheSultai()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), target.getId()), null);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(creature));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private void castSingleMode(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new WillOfTheSultai()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{mode}, List.of(targetId), null);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
