package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrepareFight.class, GrizzlyBears.class, LlanowarElves.class, Forest.class})
class PrepareFightTest extends BaseCardTest {

    @Test
    @DisplayName("Prepare untaps, pumps +2/+2, and grants lifelink until end of turn")
    void prepareUntapsPumpsAndGrantsLifelink() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new PrepareFight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
        harness.assertInGraveyard(player1, "Prepare");
    }

    @Test
    @DisplayName("Prepare boost and lifelink wear off at end of turn")
    void prepareEffectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrepareFight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Prepare cannot target a non-creature")
    void prepareCannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new PrepareFight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fight from graveyard makes creatures fight, then exiles")
    void fightFlashbackResolvesAndExiles() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new PrepareFight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castFlashback(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Prepare") || c.getName().equals("Fight"));
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Prepare"));
    }

    @Test
    @DisplayName("Fight cannot use opponent's creature as first target")
    void fightCannotTargetOpponentAsFirst() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new PrepareFight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID theirBearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID theirElvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(theirBearId, theirElvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Fight cannot use own creature as second target")
    void fightCannotTargetOwnAsSecond() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new PrepareFight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(bear1.getId(), bear2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fight requires sorcery timing")
    void fightRequiresSorceryTiming() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new PrepareFight()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    @Test
    @DisplayName("Prepare can untap and enhance an opponent's creature")
    void prepareCanTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new PrepareFight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Prepare followed by Fight uses enhanced power and grants life from fight damage")
    void prepareThenFightGainsLife() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PrepareFight()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, mine.getId());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castFlashback(player1, 0, List.of(mine.getId(), theirs.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(mine.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof PrepareFight);
    }

    @Test
    @DisplayName("Fight deals both creatures' damage before lethal damage removes them")
    void fightKillsBothCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new PrepareFight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castFlashback(player1, 0, List.of(mine.getId(), theirs.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fight deals no damage if the first target changes controllers")
    void fightDoesNothingWhenFirstTargetChangesController() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new PrepareFight()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castFlashback(player1, 0, List.of(mine.getId(), theirs.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(mine);
        gd.playerBattlefields.get(player2.getId()).add(mine);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(mine, theirs);
        assertThat(mine.getMarkedDamage()).isZero();
        assertThat(theirs.getMarkedDamage()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof PrepareFight);
    }

    @Test
    @DisplayName("Fight is still exiled if both targets leave the battlefield")
    void fightExilesWhenBothTargetsAreGone() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new PrepareFight()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castFlashback(player1, 0, List.of(mine.getId(), theirs.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(mine);
        gd.playerBattlefields.get(player2.getId()).remove(theirs);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof PrepareFight);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c instanceof PrepareFight);
    }
}
