package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.a.AuroraGriffin;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurpriseDeployment.class, AlphaKavu.class, AuroraGriffin.class, ManaCylix.class})
class SurpriseDeploymentTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a nonwhite creature onto the battlefield and returns it at the next end step")
    void putsNonwhiteCreatureAndReturnsItAtEndStep() {
        Card creature = new AlphaKavu();
        harness.setHand(player1, List.of(new SurpriseDeployment(), creature));
        castDeployment();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent entered = findPermanent(player1, "Alpha Kavu");
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(entered.getId())
                        && action.kind() == DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP);

        harness.passUntil(TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Alpha Kavu");
        harness.assertInHand(player1, "Alpha Kavu");
    }

    @Test
    @DisplayName("Does not return the creature if it left the battlefield first")
    void doesNotReturnCreatureAfterItLeavesBattlefield() {
        Card creature = new AlphaKavu();
        harness.setHand(player1, List.of(new SurpriseDeployment(), creature));
        castDeployment();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent entered = findPermanent(player1, "Alpha Kavu");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, entered));

        harness.passUntil(TurnStep.END_STEP);

        harness.assertInGraveyard(player1, "Alpha Kavu");
        harness.assertNotInHand(player1, "Alpha Kavu");
    }

    @Test
    @DisplayName("Declining leaves the nonwhite creature in hand")
    void decliningLeavesCreatureInHand() {
        harness.setHand(player1, List.of(new SurpriseDeployment(), new AlphaKavu()));
        castDeployment();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Alpha Kavu");
        harness.assertInHand(player1, "Alpha Kavu");
    }

    @Test
    @DisplayName("Cannot put a white creature onto the battlefield")
    void cannotPutWhiteCreature() {
        harness.setHand(player1, List.of(new SurpriseDeployment(), new AuroraGriffin()));
        castDeployment();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Aurora Griffin");
        harness.assertInHand(player1, "Aurora Griffin");
    }

    @Test
    @DisplayName("Cannot put a noncreature card onto the battlefield")
    void cannotPutNoncreatureCard() {
        harness.setHand(player1, List.of(new SurpriseDeployment(), new ManaCylix()));
        castDeployment();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Mana Cylix");
        harness.assertInHand(player1, "Mana Cylix");
    }

    @Test
    @DisplayName("Cannot be cast outside combat")
    void cannotCastOutsideCombat() {
        harness.setHand(player1, List.of(new SurpriseDeployment(), new AlphaKavu()));
        addDeploymentMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void castDeployment() {
        addDeploymentMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void addDeploymentMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

}
