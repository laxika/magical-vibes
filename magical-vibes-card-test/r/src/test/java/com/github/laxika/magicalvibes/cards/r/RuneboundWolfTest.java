package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.w.WyluliWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuneboundWolf.class, WyluliWolf.class, GreaterWerewolf.class, GrizzlyBears.class, Abrade.class})
class RuneboundWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Alone deals 1 damage to target opponent (counts itself)")
    void aloneDealsOne() {
        Permanent wolf = addCreatureReady(player1, new RuneboundWolf());
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counts another Wolf you control")
    void countsAnotherWolf() {
        addCreatureReady(player1, new RuneboundWolf());
        harness.addToBattlefield(player1, new WyluliWolf());
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts a Werewolf you control")
    void countsWerewolf() {
        addCreatureReady(player1, new RuneboundWolf());
        harness.addToBattlefield(player1, new GreaterWerewolf());
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not count a non-Wolf, non-Werewolf creature")
    void ignoresIrrelevantCreature() {
        addCreatureReady(player1, new RuneboundWolf());
        harness.addToBattlefield(player1, new GrizzlyBears());
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        addCreatureReady(player1, new RuneboundWolf());
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new RuneboundWolf());
        wolf.setSummoningSick(true);
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotCountOpponentsWolvesOrWerewolves() {
        addCreatureReady(player1, new RuneboundWolf());
        harness.addToBattlefield(player2, new RuneboundWolf());
        harness.addToBattlefield(player2, new GreaterWerewolf());
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void countsWolvesThatEnterBeforeResolution() {
        addCreatureReady(player1, new RuneboundWolf());
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.addToBattlefield(player1, new RuneboundWolf());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void sourceCanLeaveBeforeResolutionAndRemainingWolvesStillDealDamage() {
        Permanent source = addCreatureReady(player1, new RuneboundWolf());
        harness.addToBattlefield(player1, new RuneboundWolf());
        harness.setHand(player2, List.of(new Abrade()));
        addAbilityMana(player1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        gs.passPriority(gd, player1);
        harness.castInstant(player2, 0, 0, source.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void dealsNoDamageWhenNoWolvesRemainAtResolution() {
        Permanent source = addCreatureReady(player1, new RuneboundWolf());
        harness.setHand(player2, List.of(new Abrade()));
        addAbilityMana(player1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        gs.passPriority(gd, player1);
        harness.castInstant(player2, 0, 0, source.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void cannotActivateWhenTapped() {
        Permanent wolf = addCreatureReady(player1, new RuneboundWolf());
        wolf.tap();
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateDuringOpponentsTurn() {
        addCreatureReady(player1, new RuneboundWolf());
        addAbilityMana(player1);
        enterMainWithPriority(player2);
        gs.passPriority(gd, player2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void cannotPayRedRequirementWithOnlyColorlessMana() {
        Permanent wolf = addCreatureReady(player1, new RuneboundWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wolf.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        Permanent wolf = addCreatureReady(player1, new RuneboundWolf());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wolf.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
