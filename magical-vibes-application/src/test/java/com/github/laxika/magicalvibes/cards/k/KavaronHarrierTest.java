package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavaronHarrier.class, GiantGrowth.class})
class KavaronHarrierTest extends BaseCardTest {

    @Test
    @DisplayName("Paying creates a tapped and attacking Robot token")
    void payingCreatesTappedAttackingRobot() {
        Permanent harrier = addCreatureReady(player1, new KavaronHarrier());
        preventAutoPass(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        Permanent robot = findPermanents(player1, "Robot").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(robot.getCard().getPower()).isEqualTo(2);
        assertThat(robot.getCard().getToughness()).isEqualTo(2);
        assertThat(robot.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.isTapped()).isTrue();
        assertThat(robot.isAttackedThisTurn()).isTrue();
        assertThat(robot.getAttackTarget()).isEqualTo(harrier.getAttackTarget());
    }

    @Test
    @DisplayName("Declining the payment creates no Robot token")
    void decliningPaymentCreatesNoToken() {
        addCreatureReady(player1, new KavaronHarrier());
        preventAutoPass(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Robot"))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The created Robot token is sacrificed at end of combat")
    void tokenIsSacrificedAtEndOfCombat() {
        addCreatureReady(player1, new KavaronHarrier());
        preventAutoPass(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent robot = findPermanents(player1, "Robot").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(robot);
    }

    private void preventAutoPass(Player player) {
        harness.setHand(player, List.of(new GiantGrowth()));
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
