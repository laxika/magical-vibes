package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
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

@CardUsed({AhnCropInvader.class, GoblinAssailant.class})
class AhnCropInvaderTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn only")
    void hasFirstStrikeDuringItsControllersTurnOnly() {
        Permanent invader = addCreatureReady(player1, new AhnCropInvader());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, invader, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, invader, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing another creature gives it +2/+0 until end of turn")
    void sacrificesAnotherCreatureAndGetsBoost() {
        Permanent invader = addCreatureReady(player1, new AhnCropInvader());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int basePower = gqs.getEffectivePower(gd, invader);
        int baseToughness = gqs.getEffectiveToughness(gd, invader);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, invader)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, invader)).isEqualTo(baseToughness);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(invader).doesNotContain(goblin);
        harness.assertInGraveyard(player1, "Goblin Assailant");
    }

    @Test
    @DisplayName("The +2/+0 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent invader = addCreatureReady(player1, new AhnCropInvader());
        harness.addToBattlefield(player1, new GoblinAssailant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int basePower = gqs.getEffectivePower(gd, invader);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, invader)).isEqualTo(basePower + 2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, invader)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        addCreatureReady(player1, new AhnCropInvader());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeIsPaidBeforeBoostResolves() {
        Permanent invader = addCreatureReady(player1, new AhnCropInvader());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int basePower = gqs.getEffectivePower(gd, invader);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goblin);
        harness.assertInGraveyard(player1, "Goblin Assailant");
        assertThat(gqs.getEffectivePower(gd, invader)).isEqualTo(basePower);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, invader)).isEqualTo(basePower + 2);
    }

    @Test
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent invader = harness.addToBattlefieldAndReturn(player1, new AhnCropInvader());
        invader.setSummoningSick(true);
        invader.setTapped(true);
        harness.addToBattlefield(player1, new GoblinAssailant());
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int basePower = gqs.getEffectivePower(gd, invader);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, invader)).isEqualTo(basePower + 2);
        assertThat(gqs.hasKeyword(gd, invader, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(invader.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Goblin Assailant");
    }

    @Test
    void repeatedActivationsStackAndAllowChoosingSacrifice() {
        Permanent invader = addCreatureReady(player1, new AhnCropInvader());
        Permanent firstGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        Permanent secondGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int basePower = gqs.getEffectivePower(gd, invader);

        harness.activateAbility(player1, 0, null, null);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, invader.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, secondGoblin.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstGoblin).doesNotContain(secondGoblin);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(invader);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, invader)).isEqualTo(basePower + 2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, invader)).isEqualTo(basePower + 4);
    }

    @Test
    void opponentsCreatureCannotPaySacrificeCost() {
        Permanent invader = addCreatureReady(player1, new AhnCropInvader());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinAssailant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(invader);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(goblin);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent invader = addCreatureReady(player1, new AhnCropInvader());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(invader, goblin);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void firstStrikeKillsBlockerBeforeItDealsDamage() {
        addCreatureReady(player1, new AhnCropInvader());
        addCreatureReady(player2, new GoblinAssailant());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Ahn-Crop Invader");
        harness.assertInGraveyard(player2, "Goblin Assailant");
        harness.assertLife(player2, 20);
    }

    @Test
    void blockingOnOpponentsTurnDoesNotHaveFirstStrike() {
        addCreatureReady(player1, new AhnCropInvader());
        addCreatureReady(player2, new GoblinAssailant());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Ahn-Crop Invader");
        harness.assertInGraveyard(player2, "Goblin Assailant");
        harness.assertLife(player1, 20);
    }
}
