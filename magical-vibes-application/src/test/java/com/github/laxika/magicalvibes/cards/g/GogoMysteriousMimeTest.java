package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GogoMysteriousMime.class, GrizzlyBears.class})
class GogoMysteriousMimeTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat offers another creature you control as the copy target")
    void offersAnotherCreatureYouControl() {
        Permanent gogo = addGogo();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownCreature.getId())
                .doesNotContain(gogo.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("Accepting the copy gives Gogo and the target the temporary bonuses")
    void acceptingCopyGivesBothCreaturesBonuses() {
        Permanent gogo = addGogo();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        resolveChoice(target);

        assertThat(gogo.getCard().getName()).isEqualTo("Gogo, Mysterious Mime");
        assertThat(gqs.getEffectivePower(gd, gogo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gogo)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, gogo, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gogo.isMustAttackThisTurn()).isTrue();
        assertThat(target.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Declining the copy leaves Gogo and the target unchanged")
    void decliningCopyLeavesBothCreaturesUnchanged() {
        Permanent gogo = addGogo();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gogo.getCard().getName()).isEqualTo("Gogo, Mysterious Mime");
        assertThat(gqs.hasKeyword(gd, gogo, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gogo.isMustAttackThisTurn()).isFalse();
        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Copy and temporary bonuses wear off at end of turn")
    void copyAndBonusesWearOffAtEndOfTurn() {
        Permanent gogo = addGogo();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        resolveChoice(target);
        assertThat(gogo.getCard().getName()).isEqualTo("Gogo, Mysterious Mime");
        assertThat(gogo.isMustAttackThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.CLEANUP, harness::passBothPriorities);

        assertThat(gogo.getCard().getName()).isEqualTo("Gogo, Mysterious Mime");
        assertThat(gqs.hasKeyword(gd, gogo, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, gogo)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gogo.isMustAttackThisTurn()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    private Permanent addGogo() {
        return addCreatureReady(player1, new GogoMysteriousMime());
    }

    private void resolveChoice(Permanent target) {
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
