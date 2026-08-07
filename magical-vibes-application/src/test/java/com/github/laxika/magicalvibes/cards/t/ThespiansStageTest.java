package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThespiansStageTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the copy ability makes this land a copy of the target land")
    void becomesCopyOfTargetLand() {
        Permanent stage = harness.addToBattlefieldAndReturn(player1, new ThespiansStage());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.activateAbility(player1, 0, 1, null, forestId);
        harness.passBothPriorities();

        assertThat(stage.getCard().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("The copy keeps the copy ability and can copy another land afterwards")
    void copyRetainsTheCopyAbility() {
        Permanent stage = harness.addToBattlefieldAndReturn(player1, new ThespiansStage());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, harness.getPermanentId(player2, "Forest"));
        harness.passBothPriorities();
        assertThat(stage.getCard().getName()).isEqualTo("Forest");

        stage.untap();
        // A basic Forest declares no activated abilities of its own (its mana ability comes from the
        // land type), so the retained copy ability is now the only one on the copy.
        harness.activateAbility(player1, 0, 0, null, harness.getPermanentId(player2, "Island"));
        harness.passBothPriorities();

        assertThat(stage.getCard().getName()).isEqualTo("Island");
    }

    @Test
    @DisplayName("The copy ability cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new ThespiansStage());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
