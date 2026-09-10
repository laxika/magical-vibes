package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcererakTheArchlich.class, GrizzlyBears.class})
class AcererakTheArchlichTest extends BaseCardTest {

    @Test
    @DisplayName("Returns to its owner's hand and ventures when its controller has not completed a dungeon")
    void returnsAndVenturesBeforeDungeonCompletion() {
        castAcererak(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof AcererakTheArchlich);
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Does not trigger when its controller has completed a dungeon")
    void doesNotTriggerAfterDungeonCompletion() {
        gd.playersWhoCompletedDungeon.add(player1.getId());
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
        addCreatureReady(player2, new GrizzlyBears());

        resolveAttackTrigger();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing the opponent's only creature prevents the Zombie")
    void opponentSacrificesOnlyCreature() {
        addAttackingAcererak();
        addCreatureReady(player2, new GrizzlyBears());

        resolveAttackTrigger();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lets the opponent choose which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        addAttackingAcererak();
        addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveAttackTrigger();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, otherCreature.getId());

        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    private void castAcererak(Player player) {
        harness.setHand(player, List.of(new AcererakTheArchlich()));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castCreature(player, 0);
    }

    private Permanent addAttackingAcererak() {
        return addCreatureReady(player1, new AcererakTheArchlich());
    }

    private void resolveAttackTrigger() {
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
