package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BrilliantPlan;
import com.github.laxika.magicalvibes.cards.v.VolunteerMilitia;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PreemptiveStrike.class, VolunteerMilitia.class, BrilliantPlan.class})
class PreemptiveStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell")
    void countersCreatureSpell() {
        VolunteerMilitia militia = new VolunteerMilitia();
        harness.setHand(player1, List.of(militia));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new PreemptiveStrike()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, militia.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Volunteer Militia");
        harness.assertNotOnBattlefield(player1, "Volunteer Militia");
    }

    @Test
    @DisplayName("Cannot target a noncreature spell")
    void cannotTargetSorcerySpell() {
        BrilliantPlan brilliantPlan = new BrilliantPlan();
        harness.setHand(player1, List.of(brilliantPlan));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.setHand(player2, List.of(new PreemptiveStrike()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, brilliantPlan.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the targeted creature spell leaves the stack before resolution")
    void fizzlesIfTargetLeavesStack() {
        VolunteerMilitia militia = new VolunteerMilitia();
        harness.setHand(player1, List.of(militia));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new PreemptiveStrike()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, militia.getId());

        gd.stack.removeIf(stackEntry -> stackEntry.getCard().getId().equals(militia.getId()));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Preemptive Strike");
    }
}
