package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VodalianIllusionistTest extends BaseCardTest {

    @Test
    @DisplayName("Ability phases out the targeted creature and taps the Illusionist")
    void phasesOutTargetCreature() {
        Permanent illusionist = addCreatureReady(player1, new VodalianIllusionist());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(illusionist.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("A phased-out creature phases in during its controller's next untap step")
    void phasedOutCreaturePhasesBackIn() {
        addCreatureReady(player1, new VodalianIllusionist());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        advanceTurn(); // player2's untap step — bears phases in

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Targeting a land is rejected")
    void cannotTargetLand() {
        addCreatureReady(player1, new VodalianIllusionist());
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
