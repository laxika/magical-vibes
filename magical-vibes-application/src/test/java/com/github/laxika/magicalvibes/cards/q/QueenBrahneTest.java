package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QueenBrahne.class, Shock.class, GrizzlyBears.class})
class QueenBrahneTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Queen Brahne creates a 0/1 black Wizard token")
    void attackCreatesWizardToken() {
        addQueen();

        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent wizard = findPermanent(player1, "Wizard");
        assertThat(wizard.getCard().isToken()).isTrue();
        assertThat(wizard.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(wizard.getCard().getSubtypes()).containsExactly(CardSubtype.WIZARD);
        assertThat(gqs.getEffectivePower(gd, wizard)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Wizard tokens deal 1 damage to each opponent for a noncreature spell")
    void wizardDamagesOpponentForNoncreatureSpell() {
        createWizard();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Wizard tokens do not trigger for a creature spell")
    void wizardDoesNotTriggerForCreatureSpell() {
        createWizard();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prowess gives Queen Brahne +1/+1 for a noncreature spell")
    void prowessBoostsQueen() {
        Permanent queen = addQueen();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, queen)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, queen)).isEqualTo(2);
    }

    private Permanent createWizard() {
        addQueen();
        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Wizard");
    }

    private Permanent addQueen() {
        Permanent queen = addCreatureReady(player1, new QueenBrahne());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return queen;
    }
}
