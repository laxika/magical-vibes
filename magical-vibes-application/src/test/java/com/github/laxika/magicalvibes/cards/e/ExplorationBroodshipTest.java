package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ExplorationBroodship.class, Forest.class, GrizzlyBears.class})
class ExplorationBroodshipTest extends BaseCardTest {

    @Test
    @DisplayName("Station adds charge counters equal to another creature's power")
    void stationUsesAnotherCreaturePower() {
        Permanent broodship = harness.addToBattlefieldAndReturn(player1, new ExplorationBroodship());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(broodship), null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(broodship.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Three charge counters add a land play and eight charge counters grant flying")
    void chargeThresholdsUnlockAbilities() {
        Permanent broodship = harness.addToBattlefieldAndReturn(player1, new ExplorationBroodship());

        broodship.setCounterCount(CounterType.CHARGE, 2);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, broodship, Keyword.FLYING)).isFalse();
        assertThat(gqs.isCreature(gd, broodship)).isFalse();

        broodship.setCounterCount(CounterType.CHARGE, 3);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);

        broodship.setCounterCount(CounterType.CHARGE, 7);
        assertThat(gqs.hasKeyword(gd, broodship, Keyword.FLYING)).isFalse();
        assertThat(gqs.isCreature(gd, broodship)).isFalse();

        broodship.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.hasKeyword(gd, broodship, Keyword.FLYING)).isTrue();
        assertThat(gqs.isCreature(gd, broodship)).isTrue();
    }

    @Test
    @DisplayName("Casts one permanent from the graveyard by sacrificing a land")
    void castsPermanentFromGraveyardBySacrificingLand() {
        harness.addToBattlefield(player1, new ExplorationBroodship());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.castFromGraveyardWithSacrifice(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The graveyard permission excludes lands and is limited to one cast per turn")
    void graveyardPermissionIsFilteredAndLimited() {
        harness.addToBattlefield(player1, new ExplorationBroodship());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifice(
                player1, 0, firstLand.getId())).isInstanceOf(IllegalStateException.class);

        harness.castFromGraveyardWithSacrifice(player1, 1, firstLand.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifice(
                player1, 1, secondLand.getId())).isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
