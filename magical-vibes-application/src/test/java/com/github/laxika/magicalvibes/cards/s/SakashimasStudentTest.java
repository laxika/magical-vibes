package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SakashimasStudent.class, GrizzlyBears.class})
class SakashimasStudentTest extends BaseCardTest {

    @Test
    @DisplayName("Sakashima's Student copies a creature and adds Ninja to its types")
    void copiesCreatureAndAddsNinjaSubtype() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        SakashimasStudent student = new SakashimasStudent();
        harness.setHand(player1, List.of(student));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveCopyChoice(bears);

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == student)
                .findFirst()
                .orElseThrow();

        assertThat(copy.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(copy.getCard().getPower()).isEqualTo(2);
        assertThat(copy.getCard().getToughness()).isEqualTo(2);
        assertThat(copy.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.BEAR, CardSubtype.NINJA);
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Sakashima's Student onto the battlefield attacking")
    void ninjutsuSwapsAnUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        SakashimasStudent studentCard = new SakashimasStudent();
        harness.setHand(player1, List.of(studentCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent student = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == studentCard)
                .findFirst()
                .orElseThrow();

        assertThat(student.isTapped()).isTrue();
        harness.assertLife(player2, 18);
        assertThat(student.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(student.getCard().getSubtypes()).contains(CardSubtype.NINJA);
    }

    private void resolveCopyChoice(Permanent target) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
    }
}
