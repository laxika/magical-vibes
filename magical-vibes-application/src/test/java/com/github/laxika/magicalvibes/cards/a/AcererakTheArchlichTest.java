package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcererakTheArchlich.class, DireWolfProwler.class})
class AcererakTheArchlichTest extends BaseCardTest {

    @Test
    @DisplayName("Returns to its owner's hand and ventures when its controller has not completed a dungeon")
    void returnsAndVenturesBeforeDungeonCompletion() {
        castAcererak(player1);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof AcererakTheArchlich);
        assertThat(gd.playersWhoVenturedIntoDungeonThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("Does not trigger when its controller has completed Tomb of Annihilation")
    void doesNotTriggerAfterDungeonCompletion() {
        gd.recordCompletedDungeon(player1.getId(), Dungeon.TOMB_OF_ANNIHILATION);
        castAcererak(player1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AcererakTheArchlich);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Creates a Zombie when the opponent declines to sacrifice a creature")
    void opponentDeclinesSacrifice() {
        addAttackingAcererak();
        addCreatureReady(player2, new DireWolfProwler());

        resolveAttackTrigger();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(countPermanents(player2, "Dire Wolf Prowler")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing the opponent's only creature prevents the Zombie")
    void opponentSacrificesOnlyCreature() {
        addAttackingAcererak();
        addCreatureReady(player2, new DireWolfProwler());

        resolveAttackTrigger();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lets the opponent choose which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        addAttackingAcererak();
        addCreatureReady(player2, new DireWolfProwler());
        Permanent otherCreature = addCreatureReady(player2, new DireWolfProwler());

        resolveAttackTrigger();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, otherCreature.getId());

        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Completing Lost Mine does not prevent Acererak from returning and venturing")
    void completingAnotherDungeonDoesNotSatisfyTombCondition() {
        gd.recordCompletedDungeon(player1.getId(), Dungeon.LOST_MINE_OF_PHANDELVER);
        castAcererak(player1);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof AcererakTheArchlich);
        assertThat(gd.playersWhoVenturedIntoDungeonThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("Venture asks the controller to choose a dungeon instead of forcing Lost Mine")
    void controllerChoosesDungeonWhenNotInOne() {
        castAcererak(player1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof AcererakTheArchlich);
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Venture asks for a room choice at the Lost Mine entrance")
    void controllerChoosesNextRoomAtBranch() {
        gd.playerDungeonProgress.put(player1.getId(), new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        castAcererak(player1);

        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Creates a Zombie automatically when the opponent controls no creatures")
    void createsZombieWhenOpponentHasNoCreature() {
        addAttackingAcererak();

        resolveAttackTrigger();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(countPermanents(player2, "Zombie")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Completing Tomb before the enter trigger resolves prevents both return and venture")
    void rechecksTombCompletionOnResolution() {
        castAcererak(player1);
        harness.passBothPriorities();
        gd.recordCompletedDungeon(player1.getId(), Dungeon.TOMB_OF_ANNIHILATION);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AcererakTheArchlich);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playersWhoVenturedIntoDungeonThisTurn).doesNotContain(player1.getId());
    }

    private void castAcererak(Player player) {
        harness.castFromHand(player, new AcererakTheArchlich(), "{2}{B}");
    }

    private void addAttackingAcererak() {
        addCreatureReady(player1, new AcererakTheArchlich());
    }

    private void resolveAttackTrigger() {
        declareAttackers(List.of(0));
        resolveAllTriggers();
    }
}
