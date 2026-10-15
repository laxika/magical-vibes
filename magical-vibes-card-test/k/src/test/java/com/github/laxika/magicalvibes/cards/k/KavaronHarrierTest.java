package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.Gravkill;
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

@CardUsed({KavaronHarrier.class, Gravkill.class})
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
        assertThat(robot.getCard().getColors()).isEmpty();
        assertThat(robot.isTapped()).isTrue();
        assertThat(robot.isAttackedThisTurn()).isTrue();
        assertThat(robot.getAttackTarget()).isEqualTo(harrier.getAttackTarget());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
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
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
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

        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(robot);
    }

    @Test
    @DisplayName("End-of-combat sacrifice uses the stack and allows responses")
    void sacrificeAllowsResponses() {
        addCreatureReady(player1, new KavaronHarrier());
        preventAutoPass(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent robot = findPermanent(player1, "Robot");

        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(robot);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(robot);
    }

    @Test
    @DisplayName("A token controlled by the opponent cannot be sacrificed by its creator")
    void stolenTokenIsNotSacrificed() {
        addCreatureReady(player1, new KavaronHarrier());
        preventAutoPass(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent robot = findPermanent(player1, "Robot");
        gd.playerBattlefields.get(player1.getId()).remove(robot);
        gd.playerBattlefields.get(player2.getId()).add(robot);

        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(robot);
    }

    private void preventAutoPass(Player player) {
        harness.setHand(player, List.of(new Gravkill()));
        harness.addMana(player, ManaColor.BLACK, 4);
    }
}
