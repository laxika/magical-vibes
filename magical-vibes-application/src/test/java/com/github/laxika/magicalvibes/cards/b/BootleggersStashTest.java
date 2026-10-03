package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BootleggersStash.class, Forest.class, AshayaSoulOfTheWild.class, MarchOfTheMachines.class})
class BootleggersStashTest extends BaseCardTest {

    @Test
    void controlledLandsCanTapToCreateTreasure() {
        harness.addToBattlefield(player1, new BootleggersStash());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void doesNotGrantAbilityToOpponentLands() {
        harness.addToBattlefield(player1, new BootleggersStash());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void landsLoseGrantedAbilityWhenStashLeavesBattlefield() {
        Permanent stash = harness.addToBattlefieldAndReturn(player1, new BootleggersStash());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).remove(stash);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void newlyEnteredNoncreatureLandCanCreateTreasureUsingTheStack() {
        harness.addToBattlefield(player1, new BootleggersStash());
        Permanent forest = harness.enterBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isZero();
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
    }

    @Test
    void activatedAbilityStillResolvesAfterStashLeaves() {
        Permanent stash = harness.addToBattlefieldAndReturn(player1, new BootleggersStash());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(stash);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void tappedLandCannotCreateAnotherTreasure() {
        harness.addToBattlefield(player1, new BootleggersStash());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void stashGrantsTreasureAbilityToItselfWhenItIsALand() {
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        Permanent march = harness.addToBattlefieldAndReturn(player1, new MarchOfTheMachines());
        Permanent stash = harness.addToBattlefieldAndReturn(player1, new BootleggersStash());
        stash.setSummoningSick(false);

        assertThat(gqs.isLand(gd, stash)).isTrue();
        harness.activateAbility(player1, 2, 1, null, null);

        assertThat(stash.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(march);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void creatureLandCannotPayTapCostWhileSummoningSick() {
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        harness.addToBattlefield(player1, new BootleggersStash());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }
}
