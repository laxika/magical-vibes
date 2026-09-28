package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({HotPursuit.class, GrizzlyBears.class})
class HotPursuitTest extends BaseCardTest {

    @Test
    @DisplayName("Suspects and goads the target creature while attached")
    void suspectsAndGoadsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castHotPursuit(target);

        assertThat(target.isSuspected()).isTrue();
        assertThat(gqs.isGoaded(gd, target)).isTrue();
    }

    @Test
    @DisplayName("At the beginning of combat, steals goaded and suspected creatures")
    void stealsGoadedAndSuspectedCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        Permanent suspectedOnly = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        suspectedOnly.setSuspected(true);
        castHotPursuit(target);
        gd.playersWhoLostGameThisMatch.add(player1.getId());
        gd.playersWhoLostGameThisMatch.add(player2.getId());

        advanceToCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target, suspectedOnly);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target, suspectedOnly);
        assertThat(target.isTapped()).isFalse();
        assertThat(suspectedOnly.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, suspectedOnly, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target, suspectedOnly);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, suspectedOnly, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not steal creatures before two players have lost the game")
    void doesNotStealBeforeConditionIsMet() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castHotPursuit(target);
        gd.playersWhoLostGameThisMatch.add(player1.getId());

        advanceToCombat(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void onlyTargetsAnOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HotPursuit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void castHotPursuit(Permanent target) {
        harness.setHand(player1, List.of(new HotPursuit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
